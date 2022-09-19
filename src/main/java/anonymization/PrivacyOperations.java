package anonymization;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.OrderedDistanceTCloseness;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import models.MISP.Attribute;
import models.MISP.Event;
import models.MISP.EventMISP;
import models.MISP.Object;
import models.Policies.AttPolicy;
import models.Policies.Att_agrupation;
import models.Policies.Att_dp_param;
import models.Policies.Att_gn;
import models.Policies.Att_indv;
import models.Policies.Hierarchy;
import models.Policies.Hierarchy_Object;
import models.Policies.Pet;
import models.Policies.PetMetadata;
import models.Policies.PrivacyPolicy;
import models.Policies.Template;

import javax.naming.Context;

public class PrivacyOperations {
	
	private final String UUID = "uuid";
	private final String QUASI = "quasi";
	private final String QUASI_KANOM = "quasi/k-anonymity";
	private final String TCLOS_HIERACHICAL = "t-closeness/hierachical";
	private final String TCLOS_ORDERED = "t-closeness/ordered";
	private final String LDIV_DISTINCT = "l-diversity/distinct";
	private final String LDIV_ENTROPY = "l-diversity/entropy";
	private final String LDIV_RECURSIVE = "l-diversity/recursive";
	private final String SUPPRESION = "suppression";
	private final String GENERALIZATION = "generalization";
	//TODO: change, get it from deploy configuration or something like that
	private final String scriptLocation = "/home/juanfran/ProducerScripts/dpRelated/applyDp.py";

	private String requestbufferdp;
	private String responsebufferdp;
	public PrivacyOperations(String requestbuffer, String responsebuffer){
		this.requestbufferdp = requestbuffer;
		this.responsebufferdp = responsebuffer;
	}




	// función interesante para comprobar cualquier tipo de error que pueda haber
	// antes
	// de la ejecución de los algoritmos.
	// policy, hierarchy, attribute_policy(0)/object_policy(1)
	protected boolean check_policies_concordant(PrivacyPolicy policy, Hierarchy hierarchy, int type_policies) {
		ArrayList<String> need_hierarchy = new ArrayList<String>(
				Arrays.asList(QUASI, QUASI_KANOM, SUPPRESION, GENERALIZATION));
		//TODO/FIX: comprobar que si es conveniente que T-Closeness hierachical esté en la coleccion need hierarchy.
		ArrayList<String> alltechniques = new ArrayList<String>(Arrays.asList(QUASI, QUASI_KANOM, LDIV_DISTINCT,
				LDIV_ENTROPY, LDIV_RECURSIVE, TCLOS_ORDERED, TCLOS_HIERACHICAL, SUPPRESION, GENERALIZATION));
		boolean failed_policy = false;
		boolean failed_hierarchy = false;
		String att_failed_name = null;
		//System.out.println("DEBUG MODE " + type_policies);
		if (type_policies == 0) {
			if(!check_policy_duplicates(policy, true)) return false;//case where there is a duplicate policy for an attribute
			//System.out.println("DEBUG LENGHT AP GET ATTRIBUTES " + policy.getAttributes().size());
			for (AttPolicy ap : policy.getAttributes()) {
				//System.out.println("DEBUG ITERACION AP ");
				String technique = null;
				Pet pet = null;
				try {
					pet = ap.getPets().get(0);
					technique = pet.getScheme();
				} catch (NullPointerException e) {
					//System.out.println("DEBUG null pointer");
					return false;
				}
				if (!alltechniques.contains(technique)) {
					failed_policy = true;
					//System.out.println("DEGUB alltechniques.contains(technique)");
					break;
				}
				//System.out.println("DEBUG Technique " + technique);
				switch (technique) {
				case QUASI_KANOM:
				case QUASI: {
					PetMetadata petmetadata = pet.getMetadata();
					//System.out.println("Pet Metadata " + petmetadata.getK());
					if(petmetadata.getK() == null || petmetadata.getK() <= 0) {
						//System.out.println("DEBUG ES NULO CHECK POLICIES");
						failed_policy = true;
					}
					// check could be static, regex, interval
					if (!(hierarchy.isGeneralization(ap.getName()) || hierarchy.isRegex(ap.getName()))) {
						failed_hierarchy = true;
						att_failed_name = ap.getName();
					}
					break;
				}
				case LDIV_DISTINCT:
				case LDIV_ENTROPY: {
					PetMetadata metadata = pet.getMetadata();
					if (!(pet.getMetadata() != null && metadata != null
							&& (metadata.getL() != null && metadata.getL() > 0))) {
						failed_hierarchy = true;
						att_failed_name = ap.getName();
					}
					break;
				}
				case LDIV_RECURSIVE:
					PetMetadata metadata = pet.getMetadata();
					if (!(pet.getMetadata() != null && metadata != null && (metadata.getL() != null
							&& metadata.getL() > 0 && metadata.getC() != null && metadata.getC() > 0))) {
						failed_hierarchy = true;
						att_failed_name = ap.getName();
					}
					break;
				case TCLOS_ORDERED:
					PetMetadata mtdata = pet.getMetadata();
					if (!(pet.getMetadata() != null && mtdata != null
							&& (mtdata.getT() != null && (mtdata.getT() > 0 && mtdata.getT() < 1)))) {
						failed_hierarchy = true;
						att_failed_name = ap.getName();
					}
					break;
				case TCLOS_HIERACHICAL:
					// TODO: check could be only static and interval¿?(currently seeing)
					// TODO: check metadata of policy
					PetMetadata mt = pet.getMetadata();
					if (!((mt != null
							&& (mt.getT() != null && (mt.getT() > 0 && mt.getT() < 1)))
							&& hierarchy.isGeneralization(ap.getName()))) {
						//System.out.println("DEBUG fallo hierachical");
						failed_hierarchy = true;
						att_failed_name = ap.getName();
					}
					//System.out.println("DEBUG No fallo hierachical");
					break;
				case SUPPRESION:
					// check could be only regex
					if (!hierarchy.isRegex(ap.getName())) {
						failed_hierarchy = true;
						att_failed_name = ap.getName();
					}
					break;
				case GENERALIZATION:
					// check could be static or interval
					//System.out.println("DEBUG generalization check policies");
					if (!hierarchy.isGeneralization(ap.getName())) {
						failed_hierarchy = true;
						att_failed_name = ap.getName();
					}
					break;
				default:
					// nothing because no comprobation needed

					break;
				}
				//System.out.println("DEBUG antes de siguiente iteracion");
				if (failed_hierarchy) {
					//System.out.println("DEGUB FAILED HIERARCHY");
					break;
					}
				//System.out.println("DEBUG SIGUIENTE ITERACION");
			}
			// TODO: check_failed hierarchy
			if (failed_policy || failed_hierarchy)
				return false;
		} else if (type_policies == 1) {
			if(!check_policy_duplicates(policy,false)) return false;//there is a duplicate policy for an attribute
			//System.out.println("DEBUG check policies concordant object");
			for (Template t : policy.getTemplates()) {
				for (AttPolicy ap : t.getAttributes()) {
					//System.out.println("Anonymizer.java check_policy_concordant " + ap.getName());
					String technique = null;
					Pet pet = null;
					try {
						pet = ap.getPets().get(0);
						technique = pet.getScheme();
					} catch (NullPointerException e) {
						return false;
					}
					if (!alltechniques.contains(technique)) {
						failed_policy = true;
						break;
					}
					switch (technique) {
					case QUASI_KANOM:
					case QUASI: {
						// check could be static, regex, interval
						if (!(t.iskAnonimity() && t.getK() > 0)) {
							//System.out.println("Hierarchy.java no k o k <= 0");
							failed_policy = true;
							break;
						}
						if (!(hierarchy.isGeneralization(t.getName(), ap.getName())
								|| hierarchy.isRegex(t.getName(), ap.getName()))) {
							//System.out.println("DEBUG failed is Generalization K-anonymity");
							failed_hierarchy = true;
							att_failed_name = ap.getName();
						}
						break;
					}
					case LDIV_DISTINCT:
					case LDIV_ENTROPY: {
						PetMetadata metadata = pet.getMetadata();
						if (!( metadata != null
								&& (metadata.getL() != null && metadata.getL() > 0))) {
							failed_hierarchy = true;
							att_failed_name = ap.getName();
						}
						break;
					}
					case LDIV_RECURSIVE:
						PetMetadata metadata = pet.getMetadata();
						if (!( metadata != null && (metadata.getL() != null
								&& metadata.getL() > 0 && metadata.getC() != null && metadata.getC() > 0))) {
							failed_hierarchy = true;
							att_failed_name = ap.getName();
						}
						break;
					case TCLOS_ORDERED:
						PetMetadata mtdata = pet.getMetadata();
						if (!(pet.getMetadata() != null /*&& mtdata != null*/
								&& (mtdata.getT() != null && (mtdata.getT() > 0 && mtdata.getT() < 1)))) {
							failed_hierarchy = true;
							att_failed_name = ap.getName();
						}
						break;
					case TCLOS_HIERACHICAL:
						// TODO: check could be only static and interval¿?(currently seeing)
						// TODO: check metadata of policy
						PetMetadata mt = pet.getMetadata();
						if (!((pet.getMetadata() != null /*&& mt != null*/
								&& (mt.getT() != null && (mt.getT() > 0 && mt.getT() < 1)))
								&& hierarchy.isGeneralization(t.getName(), ap.getName()))) {
							//TODO/FIX: comprobar si .isGeneralization es la mejor opcion. Porque he testeado tipo categórico pero nunca con T-Closeness con intervalo.
							failed_hierarchy = true;
							att_failed_name = ap.getName();
						}
						break;
					case SUPPRESION:
						// check could be only regex
						if (!hierarchy.isRegex(t.getName(), ap.getName())) {
							failed_hierarchy = true;
							att_failed_name = ap.getName();
						}
						break;
					case GENERALIZATION:
						// check could be static or interval
						if (!hierarchy.isGeneralization(t.getName(), ap.getName())) {
							failed_hierarchy = true;
							att_failed_name = ap.getName();
						}
						break;
					default:
						// nothing because no comprobation needed
						//System.out.println("Anonymizer.java default switch");
						failed_hierarchy = true;
						break;
					}
					if (failed_hierarchy)
						return false;
				}
				if (failed_policy) {
					//System.out.println("Failed policy");
					return false;
				}
			}
		} else {
			// policy thats not 0 or 1, so its a unknown type, return false
			return false;
		}
		// 4º para cada técnicas checkear que los atributos necesarios son no nulos -
		// HECHO ARRIBA

		// TODO: 5º para cada jerarquía comprobar el formato, e incluso nº de niveles
		// etc - poibilidad de checkear que todas tengan el mismo
		// número de niveles - TIRAS ERROR EN EL CATCH DE LA EJECUCIÓN DE LA LIBRERÍA Y
		// YASTA
		return true;
	}

	//when objects - per objet 1 policy
	//check that attributes only apply 1 policy
	//this function check that only 1 policy is applied to every individual attribute
	public boolean check_policy_duplicates(PrivacyPolicy policy, boolean onlyattributes){
		if(onlyattributes){
			ArrayList<String> atts = new ArrayList<>();
			for(AttPolicy a: policy.getAttributes()){
				//TODO: not todo, just important. Here we pass the situation of pets being [{}], that is check more ahead in the function
				if(a.getPets().size() > 0 && a.isDp()) return false;
				if(atts.contains(a.getName())) return false;
				atts.add(a.getName());
			}
		}else{
			for(Template object_policy : policy.getTemplates()){
				ArrayList<String> atts = new ArrayList<>();
				for(AttPolicy a : object_policy.getAttributes()){
					if(atts.contains(a.getName())) return false;
					atts.add(a.getName());
				}
				if(object_policy.getAtt_aggroupations() != null){
					for(Att_agrupation ag : object_policy.getAtt_aggroupations()){
						for(String att_name : ag.getAtt_names()){
							if(atts.contains(att_name)) return false;
							atts.add(att_name);
						}
					}
				}
			}
		}
		return true;
	}
	//when attributes - per attribute 1 policy

	
	//función que busca si es necesario aplicar dprivacy 
//	public void check_apply_dp(Event e) {
//		HashMap<String, ArrayList<String>> obj_attributes = new HashMap<String, ArrayList<String>>();
//		for(Object o : e.getObject()) {
//			if(o.get)
//		}
//	}
	//
	
	// puede retornar un objeto que sea instancia de EventMISP, nulo en caso de
	// error intermedio o un String con el error
	// producido en caso de que haya sido un error en la ejecución del algoritmo
	public java.lang.Object apply_privacy(Event event, PrivacyPolicy pol, models.Policies.Hierarchy hier,
			boolean only_attributes, PrintWriter out) {
		System.out.println("Unprotected Event " + event.toJsonString());
		long ianon = System.currentTimeMillis();
		// check if attr or objects
		if (only_attributes) {
			HashMap<String, Integer> att_kanon = new HashMap<String, Integer>();

			// check for which attributes we have a generalization available
			ArrayList<String> hasHierarchy = new ArrayList<String>();
			for (Att_indv ai : hier.getHierarchyAttributes()) {
				hasHierarchy.add(ai.getAttributeName());
			}

			// att name and pet we must apply
			HashMap<String, String> att_pet = pol.attNamePet();
			HashMap<String, String> attribute_hierarchy = hier.attgetTypes();
			HashMap<String, Integer> att_level = pol.attLevelSupression();

			ArrayList<String> quasi = new ArrayList<String>(); // quasi k-anonimity

			// replacing suppresion and generalization value
			replaceSuppresionGeneralization(event, att_level, att_pet, hier);
			// ---------------------------------------------------------

			att_pet.forEach((name, pet) -> {
				if (pet.contains("k-anonimity") && hasHierarchy.contains(name)) {
					// if has policy and hierarchy apply
					quasi.add(name);
				} else if (pet.contains("k-anonimity") && !hasHierarchy.contains(name)) {
					// TODO: esto es un error, una politica que indica aplicar k-anonymity
					// TODO: pero no hay jerarquia que lo permita
					//-> hacer tratamiento de error en clase Anonymizer <--return "no hierarchy available for attribute " + name +  " policy";
				}
			});

			for (String attname : quasi) {
				int k = pol.attGetK(attname);
				if (k == 0) {
					// TODO: error que no deberÃ­a suceder, de hecho, no hace falta ni comprobarlo
					// porque ya lo has comprobado antes, borrar si quieres
					//o también devolver en caso de que se cuele return "k no available for " + attname + " attribute";
					return null;
				}
				att_kanon.put(attname, k);
			}
			
			for(String a :att_kanon.keySet()) {
				//System.out.println("ENTRY " + a);
			}
			for (Map.Entry<String, Integer> entry : att_kanon.entrySet()) {
				// apply transformation for each attribute
				Data.DefaultData data = Data.create();

				data.add(UUID, entry.getKey());
				data.getDefinition().setDataType(entry.getKey(), DataType.STRING);
				data.getDefinition().setAttributeType(entry.getKey(), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);

				// the uuid attribute
				data.getDefinition().setDataType(UUID, DataType.STRING);
				data.getDefinition().setAttributeType(UUID, AttributeType.INSENSITIVE_ATTRIBUTE);

				String type = hier.getTypeOfAttribute(entry.getKey());
				if (type == null) {
					// fail that shouldnt happen TODO: check if error is managed before in check format or check policies concordant
				}

				AttributeType.Hierarchy.DefaultHierarchy hierarchy = AttributeType.Hierarchy.create();
				if (type.equals("static")) {
					// add static generalizations
					Att_indv ai = hier.getAttIndv(entry.getKey());
					for (Att_gn g : ai.getAttributeGeneralization()) {
						// add each level
						hierarchy.add(g.getGeneralization().toArray(new String[0]));
					}
					for (Attribute a : event.getAttributes()) {
						// add value to dataset
						if (a.getObject_relation().equals(entry.getKey())) {
							String uuid = a.getUuid();
							if (uuid == null || uuid.equals("")) {
								uuid = java.util.UUID.randomUUID().toString();
								a.setUuid(uuid);
							}
							data.add(uuid, a.getValue());
							//System.out.println("data.add(" + uuid + " ," + a.getValue() + ")");
						}
					}
					data.getDefinition().setAttributeType(entry.getKey(), hierarchy);
				} else {
					// retrieve dynamic hierarchies
					boolean f_regex = type.equals("regex");
					boolean f_interval = type.equals("interval");
					// dynamic match and replace for *
					Att_indv ai = hier.getAttIndv(entry.getKey());
					for (Attribute a : event.getAttributes()) {
						if (a.getObject_relation().equals(entry.getKey())) {
							String uuid = a.getUuid();
							if (uuid == null || uuid.equals("")) {
								uuid = java.util.UUID.randomUUID().toString();
								a.setUuid(uuid);
							}
							data.add(uuid, a.getValue());
							String[] t;
							if (f_regex) {
								t = generate_regex_hierarchy(ai, a);
								hierarchy.add(t);
							} else if (f_interval) {
								t = generate_interval_hierarchy(ai, a);
								hierarchy.add(t);
							} /*
								 * else if(f_supression){ //supression in all values, set att value to the
								 * result of applying regex over original value String new_att_value =
								 * getSupression(ai, a, att_level.get(entry.getKey()));
								 * a.setValue(new_att_value); }else if(f_generalization){ //generalization in
								 * all values, 2 options, interval o static String check_type =
								 * attribute_hierarchy.get(entry.getKey()); switch (check_type){ case
								 * "interval": String interval = generalize_interval(ai, a,
								 * att_level.get(entry.getKey())); a.setValue(interval); break; case "static":
								 * 
								 * break; default: break; }
								 * 
								 * }
								 */

						}
					}
					data.getDefinition().setAttributeType(entry.getKey(), hierarchy); // add hierachy to the definition

				}
				// execute algorithm
				ARXConfiguration config = ARXConfiguration.create();
				//System.out.println("K-anon " + att_kanon.get(entry.getKey()));
				config.addPrivacyModel(new KAnonymity(entry.getValue()));
				// config.setSuppressionLimit(0.02d);// hardcodeado, quitar
				ARXAnonymizer anonymizer = new ARXAnonymizer();
				ARXResult result = null;
				try {
					result = anonymizer.anonymize(data, config);
				} catch (Exception e) {
					// TODO: ERROR ANONIMIZACIÃ“N, DEVOLVER ERROR PARA ATRÃ�S
					return new String(e.getMessage());
				}

				// set the values
				setNewValuesAttributes(result, data, event);
			}
			long inicioenvio = System.currentTimeMillis();
			return new EventMISP(event);
		} else {
			// anonimizar objetos
			// type of object with all posible attributes
			long inicio = System.currentTimeMillis();
			HashMap<String, ArrayList<String>> typeobj_attributes = new HashMap<String, ArrayList<String>>();
			for (Object o : event.getObject()) {
				if (!typeobj_attributes.containsKey(o.getName())) {
					typeobj_attributes.put(o.getName(), new ArrayList<String>());
				}
				for (Attribute a : o.getAttribute()) {
					String att_name = a.getObject_relation();
					if (!typeobj_attributes.get(o.getName()).contains(att_name)) {
						typeobj_attributes.get(o.getName()).add(att_name);
						//System.out.println("AÃ±adiendo " + att_name + " a " + o.getName());
					}
				}
			}

			for (Map.Entry<String, ArrayList<String>> entry : typeobj_attributes.entrySet()) {
				// name of quasi att, type of quasi
				// check object has policy and hierarchy
				Template objectPolicy = pol.getPolicyObject(entry.getKey());
				Hierarchy_Object objectHierarchy = hier.getHierarchyObject(entry.getKey());
				HashMap<String, String> atts_quasi = null;
				ArrayList<String> quasi_and_hierachical = null;
				boolean format_correct = true;
				boolean hasPolicies = false;
				if (objectHierarchy != null && objectPolicy != null) {
					hasPolicies = true;
					atts_quasi = objectHierarchy.getAttributesNameType();
					// really we should only use hierarchies of attributes indicated quasi on the
					// politics
					quasi_and_hierachical = objectPolicy.getQuasiAndHierachical();
					for (String att : quasi_and_hierachical) {
						if (!atts_quasi.containsKey(att)) {
							// hierarchy file does not contain hierarchy for a object
							format_correct = false;
							break;
						}
					}
				}

				if (hasPolicies && format_correct) {

					ArrayList<String> quasi = objectPolicy.getQuasi(); // list of attributes quasi acording policy
					// HashMap<String, ArrayList<String>> sensitive_pet =
					// objectPolicy.getSensitiveAndPet();
					ArrayList<String> sensitive_pet = objectPolicy.getSensitive();
					// creating data for the algorithm
					Data.DefaultData data = Data.DefaultData.create();

					// uuid as insensitive
					data.getDefinition().setDataType(UUID, DataType.STRING);
					data.getDefinition().setAttributeType(UUID, AttributeType.INSENSITIVE_ATTRIBUTE);

					// we add uuid as first attribute in dataset
					String[] att_names = typeobj_attributes.get(entry.getKey()).toArray(new String[0]);
					String[] att_names_row = new String[att_names.length + 1];
					att_names_row[0] = UUID;
					for (int i = 1; i < att_names_row.length; i++) {
						att_names_row[i] = att_names[i - 1];
					}

					data.add(att_names_row); // add first row, names of attributes
					ArrayList<String> all_atts = new ArrayList<String>(entry.getValue());
					for (String s : quasi) {
						data.getDefinition().setDataType(s, DataType.STRING); // set data type
						data.getDefinition().setAttributeType(s, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE); // set
						// quasi
						// attributes
					}
					for (String s : sensitive_pet) {
						//System.out.println("SENSITIVE " + s);
						data.getDefinition().setDataType(s, DataType.STRING); // set data type
						data.getDefinition().setAttributeType(s, AttributeType.SENSITIVE_ATTRIBUTE);
					}
					all_atts.removeAll(quasi); // remove all from quasi and sensitive, to get insensitive
					all_atts.removeAll(sensitive_pet);
					for (String s : all_atts) {
						// set insensitive
						data.getDefinition().setDataType(s, DataType.STRING);
						data.getDefinition().setAttributeType(s, AttributeType.INSENSITIVE_ATTRIBUTE);
					}

					// here are the quasi and t-closeness hierachical attributes
					HashMap<String, AttributeType.Hierarchy.DefaultHierarchy> att_hierarchy = new HashMap<String, AttributeType.Hierarchy.DefaultHierarchy>();
					for (String a : quasi_and_hierachical) {
						att_hierarchy.put(a, AttributeType.Hierarchy.create());
					}
					// ______> estaticas
					this.setStaticHierarchies(hier, entry.getKey(), quasi_and_hierachical, att_hierarchy);

					// _______> jerarquias dinamicas
					this.setDinamicHierarchies(event, entry.getKey(), entry.getValue(), objectHierarchy, objectPolicy,
							quasi_and_hierachical, atts_quasi, att_hierarchy, data);

					// add hierarchies to data algorithm
					att_hierarchy.forEach((att, hierarchy) -> {
						if (!sensitive_pet.contains(att)) { // because we have to set the quasi one's. The sensitive
							// hierachical t-closeness is done in privacy declaration
							data.getDefinition().setAttributeType(att, hierarchy);
						}
					});
					
					ARXResult result = null;
					if(quasi.size() > 0 || sensitive_pet.size() > 0) {
					ARXConfiguration config = ARXConfiguration.create();
					// getting k of object
					int k_of_object = 0;
					for (Template t : pol.getTemplates()) {
						if (t.getName().equals(entry.getKey())) {
							k_of_object = t.getKofObject();
							break;
						}
					}
					if (k_of_object > 0) {
						// apply k-anonimity
						// config.setSuppressionLimit(0.02d);//hardcodeado, quitar
						config.addPrivacyModel(new KAnonymity(k_of_object));
					}
					//System.out.println("K - " + k_of_object);

					// setting sensitive config
					for (String nameatt : sensitive_pet) {
						this.setSensitivePet(nameatt, entry.getKey(), pol, att_hierarchy, config);
					}

					ARXAnonymizer anonymizer = new ARXAnonymizer();
					try {
						result = anonymizer.anonymize(data, config);
					} catch (Exception e) {
						// TODO: ERROR ANONIMIZACIÃ“N, DEVOLVER PARA ATRÃ�S
						return new String(e.getMessage());
					}
					// replace values in objects of event
					}
					setNewValueObjects(result, data, event, att_names_row);
					
				}
			}
			return new EventMISP(event);
		}

	}
	
	private void setSensitivePet(String nameatt, String k, PrivacyPolicy pol,
			HashMap<String, AttributeType.Hierarchy.DefaultHierarchy> att_hierarchy, ARXConfiguration config) {
		Pet pet_to_apply = null;
		for (Template t : pol.getTemplates()) {
			if (t.getName().equals(k)) {
				pet_to_apply = t.getAttribute(nameatt).getPets().get(0);
				break;
			}
		}
		switch (pet_to_apply.getScheme()) {
		case "t-closeness/hierachical":
			AttributeType.Hierarchy.DefaultHierarchy dh = att_hierarchy.get(nameatt);
			// 0.3 is hardocoded, find the value in policy
			double t = pet_to_apply.getMetadata().getT();
			config.addPrivacyModel(new HierarchicalDistanceTCloseness(nameatt, t, dh));
			//System.out.println("AÃ‘ADIENDO PRIVACY MODEL Hierachical");
			break;
		case "t-closeness/ordered":
			// find value in policy
			double tordered = pet_to_apply.getMetadata().getT();
			config.addPrivacyModel(new OrderedDistanceTCloseness(nameatt, tordered));
			//System.out.println("AÃ‘ADIENDO PRIVACY MODEL OrderedDistanceTCloseness");
			break;
		case "l-diversity/distinct":
			// find value in policy
			int l = pet_to_apply.getMetadata().getL();
			config.addPrivacyModel(new DistinctLDiversity(nameatt, l));
			//System.out.println("AÃ‘ADIENDO PRIVACY MODEL DistinctLDiversity");
			break;
		case "l-diversity/entropy":
			// find value in policy
			int lentropy = pet_to_apply.getMetadata().getL();
			config.addPrivacyModel(new EntropyLDiversity(nameatt, lentropy));
			//System.out.println("AÃ‘ADIENDO PRIVACY MODEL EntropyLDiversity");
			break;
		case "l-diversity/recursive":
			// find values in policy
			int lrec = pet_to_apply.getMetadata().getL();
			int crec = pet_to_apply.getMetadata().getC();
			config.addPrivacyModel(new RecursiveCLDiversity(nameatt, crec, lrec));
			//System.out.println("AÃ‘ADIENDO PRIVACY MODEL RecursiveCLDiversity");
			break;
		default:
			break;
		}
	}
	
	// for object of event of objects
	private void replaceSuppresionGeneralization(Object object, HashMap<String, Integer> att_level,
			HashMap<String, String> att_pet, models.Policies.Hierarchy hier) {
		for (Attribute attribute : object.getAttribute()) {
			String attribute_name = attribute.getObject_relation();
			if (att_level.containsKey(attribute_name)) {
				String pet = att_pet.get(attribute_name);
//				if (pet.equals("suppression")) {
//					System.out.println("Tiene que pasar por aqui");
//					Att_indv ai = hier.getAttIndv(attribute_name);
//					if (!ai.getAttributeType().equals("regex")) {
//						// TODO: tirar error, la supresion debe ir por
//						// TODO: regex.
//					}
//					String replace = getSupression(hier.getAttIndv(attribute_name), attribute,
//							att_level.get(attribute_name));
//					attribute.setValue(replace);
//				}
				switch (pet) {
				case "suppression":
					// TODO: comprobaciÃ³n de tipo de jerarquÃ­a correcto
					Att_indv ai = hier.getAttIndv(attribute_name);
					if (!ai.getAttributeType().equals("regex")) {
						// TODO: tirar error, la supresion debe ir por
						// TODO: regex.
					}
					String replace = getSupression(hier.getAttIndv(attribute_name), attribute,
							att_level.get(attribute_name));
					attribute.setValue(replace);
					break;
				case "generalization":
					Att_indv hi = hier.getAttIndv(attribute_name);
					if (hi.getAttributeType().equals("static")) {
						String replc = getGeneralization(hi, attribute, att_level.get(attribute_name));
						attribute.setValue(replc);
					} else if (hi.getAttributeType().equals("interval")) {
						String interval = generalize_interval(hi, attribute, att_level.get(attribute_name));
						attribute.setValue(interval);
					} else {
						// TODO: error porque deberÃ­a de ser de estos tipos
					}
					break;
				default:
					// TODO: no tiene porque entrar aqui
					break;
				}
			}
		}
	}
	
	private String getGeneralization(Att_indv ai, Attribute a, Integer level) {
		for (Att_gn a_gn : ai.getAttributeGeneralization()) {
			if (a_gn.getGeneralization().get(0).equals(a.getValue())) {
				// sustituir
				String rturn;
				try {
					rturn = a_gn.getGeneralization().get(level - 1);
				} catch (IndexOutOfBoundsException e) {
					return null;
				}
				return rturn;
			}
		}
		return null;
	}
	
	private void replaceSuppresionGeneralization(Event event, HashMap<String, Integer> att_level,
			HashMap<String, String> att_pet, models.Policies.Hierarchy hier) {
		//System.out.println("ENTRAR ENTRA EH MUCHACHO");
		for (Attribute attribute : event.getAttributes()) {
			//System.out.println("A");
			String attribute_name = attribute.getObject_relation();
			if (att_level.containsKey(attribute_name)) {
				//System.out.println("B");
				String pet = att_pet.get(attribute_name);
				if (pet.equals("suppression")) {
					//System.out.println("Suppresion");
				}
				switch (pet) {
				case "suppression":
					// TODO: comprobaciÃ³n de tipo de jerarquÃ­a correcto
					//System.out.println("Tiene que entrar aqui");
					Att_indv ai = hier.getAttIndv(attribute_name);
					if (!ai.getAttributeType().equals("regex")) {
						// TODO: tirar error, la supresion debe ir por
						// TODO: regex.
					}
					String replace = getSupression(hier.getAttIndv(attribute_name), attribute,
							att_level.get(attribute_name));
					//System.out.println("Seteando suppresion " + replace + " en valor de atributo "
					//+ attribute.getObject_relation());
					attribute.setValue(replace);
					break;
				case "generalization":
					Att_indv hi = hier.getAttIndv(attribute_name);
					if (hi.getAttributeType().equals("static")) {
						String replc = getGeneralization(hi, attribute, att_level.get(attribute_name));
						attribute.setValue(replc);
					} else if (hi.getAttributeType().equals("interval")) {
						String interval = generalize_interval(hi, attribute, att_level.get(attribute_name));
						attribute.setValue(interval);
					} else {
						// TODO: error porque deberÃ­a de ser de estos tipos
					}
					break;
				default:
					// TODO: no tiene porque entrar aqui
					break;
				}
			}
		}
	}
	
	
	// return new att value after aplying correspondant regex to original att value
	private String getSupression(Att_indv ai, Attribute a, Integer level) {
		try {
			String regex = ai.getAttributeGeneralization().get(0).getRegex().get(level - 1);
			String value = a.getValue();
			String problems = "(" + regex + ")";
			Pattern p = Pattern.compile(problems);
			Matcher m = p.matcher(value);
			if (m.find()) {
				//System.out.println("SI TIENE QUE MATCHEAR " + m.group(1));
			}
			//TODO: CORREGIR - CUANDO SE HACEN GRUPOS EN LA EXPRESION REGULAR DA ERROR
			String replace = value.replaceAll(regex, "*".repeat(m.group(1).length()));
			//System.out.println("REPLACE " + replace);
			return replace;
		} catch (Exception e) {
			// null pointer or every other error
			return null;
		}
	}
	
	
	private void setDinamicHierarchies(Event event, String k, ArrayList<String> v, Hierarchy_Object ho,
			Template object_policy, ArrayList<String> quasi_and_hierachical, HashMap<String, String> atts_quasi,
			HashMap<String, AttributeType.Hierarchy.DefaultHierarchy> att_hierarchy, Data.DefaultData data) {
		// jerarquÃ­a aÃ±adida
		// recorremos evento para recoger objetos de ese tipo
		for (Object o : event.getObject()) {
			if (o.getName().equals(k)) {
				String[] value_elements = new String[v.size() + 1]; // becacuse of uuid added in dataset
				// get or create uuid for identifiying a row
				String uuid = o.getUuid();
				if (uuid == null || uuid.equals("")) {
					uuid = java.util.UUID.randomUUID().toString();
					o.setUuid(uuid);
				}
				value_elements[0] = uuid;

				for (Attribute a : o.getAttribute()) {
					// for(String s : v ) {
					// System.out.println("V " + s);
					// }
					//System.out.println("Object relation " + a.getObject_relation() + v.indexOf(a.getObject_relation()));
					// TODO: -> antes de aquÃ­ setear valores de supresion y generalizaciÃ³n
					Integer suppresion_level;
					//
					value_elements[v.indexOf(a.getObject_relation()) + 1] = a.getValue(); // beacuse of uuid added in
					// dataset
					if (quasi_and_hierachical.contains(a.getObject_relation())
							&& atts_quasi.get(a.getObject_relation()).equals("regex")) {
						Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
						// TODO
						String[] t = generate_regex_hierarchy(ai, a);
						// hierarchy.add(t);
						att_hierarchy.get(a.getObject_relation()).add(t);
						// me he quedado aqui
					} else if (quasi_and_hierachical.contains(a.getObject_relation())
							&& atts_quasi.get(a.getObject_relation()).equals("interval")) {
						Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
						String[] t = generate_interval_hierarchy(ai, a);
						System.out.println("Hierarchy generated for " + a.getObject_relation() + " ");
						for(String in : t){
							System.out.println(" - " + in);
						}
						// hierarchy.add(t);
						att_hierarchy.get(a.getObject_relation()).add(t);
					} else if ((suppresion_level = object_policy.isSuppresion(k, a.getObject_relation())) != null) {
						// TODO: mejorar en eficiencia en estos dos ultimos if - NECESARIO
						// TODO: mejorable en eficiencia, ya que la funciÃ³n is suppresion recorre la
						// estructura de pol
						// es suppresion
						Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
						String replace = getSupression(ai, a, suppresion_level);
						if (replace == null) {
							// TODO: error
						}
						// set new value
						value_elements[v.indexOf(a.getObject_relation()) + 1] = replace;
						// a.setValue(replace);
					} else if ((suppresion_level = object_policy.isGeneralization(k, a.getObject_relation())) != null) {
						//System.out.println("Si que entra a generalizar");
						// TODO: mejorar en eficiencia en estos dos ultimos if - NECESARIO
						// caso de atributos cuya polÃ­tica sea de generalizacion
						Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
						switch (ai.getAttributeType()) {
						case "static":
							String replace = getGeneralization(ai, a, suppresion_level);
							//System.out.println("Ahi te va maestro " + replace);
							value_elements[v.indexOf(a.getObject_relation()) + 1] = replace;
							// a.setValue(replace);
							break;
						case "interval":
							String rplace = generalize_interval(ai, a, suppresion_level);
							value_elements[v.indexOf(a.getObject_relation()) + 1] = rplace;
							// a.setValue(rplace);
							break;
						default:
							// TODO: no deberia entrar aquÃ­, si lo hace es por un error en el fichero de
							// jerarquÃ­as
							break;
						}
					}
				}
				data.add(value_elements);
			}
		}
	}
	
	private String[] generate_regex_hierarchy(Att_indv ai, Attribute a) {
		// add hierarchy for regex
		Att_gn g = ai.getAttributeGeneralization().get(0);
		// System.out.println("G " + g.getRegex().toString());
		ArrayList<String> transform = new ArrayList<String>();
		String value = a.getValue();
		//System.out.println("data.add(" + value + ")");
		// transform.add(value);
		transform.add(value);
		for (String regex : g.getRegex()) {
			// System.out.println("REGEX-" + regex);
			transform.add(value.replaceAll(regex, "*"));
		}
		String[] t = transform.toArray(new String[0]);
		// System.out.println("T " + t);
		for (String it : t) {
			//System.out.println("IT aindv " + ai.getAttributeName() + " " + it);
		}
		return t;
	}
	
	// return string that references interval that replace original value
	private String generalize_interval(Att_indv ai, Attribute a, Integer level) {
		Pattern lessthan = Pattern.compile("<([0-9]+)");
		Pattern lessEqualthan = Pattern.compile("<=([0-9]+)");
		Pattern biggerthan = Pattern.compile(">([0-9]+)");
		Pattern biggerEqualthan = Pattern.compile(">=([0-9]+)");
		Pattern interval = Pattern.compile("([0-9]+)-([0-9]+)");
		String rturn = null;
		Att_gn g = ai.getAttributeGeneralization().get(level);
		// add the level of generalization according number
		for (String comparison : g.getInterval()) {
			Matcher mlt = lessthan.matcher(comparison);
			Matcher mlet = lessEqualthan.matcher(comparison);
			Matcher mbt = biggerthan.matcher(comparison);
			Matcher mbet = biggerEqualthan.matcher(comparison);
			Matcher mi = interval.matcher(comparison);
			Integer number_value = Integer.valueOf(a.getValue());
			if (mlt.matches()) {
				if (number_value < Integer.valueOf(mlt.group(1))) {
					rturn = comparison;
					//System.out.println("AÃ±adiendo < que");
					break;
				}
			} else if (mlet.matches()) {
				if (number_value <= Integer.valueOf(mlet.group(1))) {
					rturn = comparison;
					//System.out.println(number_value + "AÃ±adiendo <= que");
					break;
				}
			} else if (mbt.matches()) {
				if (number_value > Integer.valueOf(mbt.group(1))) {
					rturn = comparison;
					//System.out.println(number_value + "AÃ±adiendo > que");
					break;
				}
			} else if (mbet.matches()) {
				if (number_value >= Integer.valueOf(mbet.group(1))) {
					rturn = comparison;
					//System.out.println(number_value + "AÃ±adiendo >= que");
					break;
				}
			} else {
				// interval
				mi.matches();
				System.out.println("Comparison " + comparison);
				System.out.println("Number value " + number_value);
				System.out.println("Match " + mi.group(1) + mi.group(2));
				if (number_value >= Integer.valueOf(mi.group(1)) && number_value <= Integer.valueOf(mi.group(2))) {
					rturn = comparison;
					//System.out.println(number_value + "AÃ±adiendo intervalo " + comparison);
					break;
				}
			}
		}
		return rturn;
	}
	
	
	private boolean compare_int_double(String a, String b, String c,boolean is_int, int operator) {
		if(is_int) {
			Integer v1 = Integer.valueOf(a);
			Integer v2 = Integer.valueOf(b);
			Integer v3 = null;
			if(c != null) {
				v3 = Integer.valueOf(c);
			}
			switch (operator) {
			case 0:
				return v1 < v2;
			case 1:
				return v1 <= v2;
			case 2:
				return v1 > v2;
			case 3:
				return v1 >= v2;
			case 4:
				return v1 >= v2 && v1 <= v3;
			default:
				break;
			}
		}else {
			Double v1 = Double.valueOf(a);
			Double v2 = Double.valueOf(b);
			Double v3 = null;
			if(c!=null) {
				v3 = Double.valueOf(c);
			} 
			switch (operator) {
			case 0:
				return v1 < v2;
			case 1:
				return v1 <= v2;
			case 2:
				return v1 > v2;
			case 3:
				return v1 >= v2;
			case 4:
				return v1 >= v2 && v1 <= v3;
			default:
				break;
			}
		}
		return false;
	}
	
	private String[] generate_interval_hierarchy(Att_indv ai, Attribute a) {
		Pattern lessthan = Pattern.compile("<([0-9]+)");
		Pattern lessEqualthan = Pattern.compile("<=([0-9]+)");
		Pattern biggerthan = Pattern.compile(">([0-9]+)");
		Pattern biggerEqualthan = Pattern.compile(">=([0-9]+)");
		Pattern interval = Pattern.compile("([0-9]+)-([0-9]+)");
		// patterns
		ArrayList<String> transform = new ArrayList<String>();
		transform.add(a.getValue());
		for (Att_gn g : ai.getAttributeGeneralization()) {
			// add the level of generalization according number
			for (String comparison : g.getInterval()) {
				Matcher mlt = lessthan.matcher(comparison);
				Matcher mlet = lessEqualthan.matcher(comparison);
				Matcher mbt = biggerthan.matcher(comparison);
				Matcher mbet = biggerEqualthan.matcher(comparison);
				Matcher mi = interval.matcher(comparison);
				String raw_value = a.getValue();
				boolean iexcept = false;
				boolean dexcept = false;
				boolean is_integer = false;
				boolean is_double = false; 
				double dvalue;
				int ivalue;
				try {
					dvalue = Double.valueOf(raw_value);
					is_double = true;
				} catch (NumberFormatException e) {
					dexcept = true;
				}
				try {
					ivalue = Integer.valueOf(raw_value);
					is_integer = true;
				} catch (NumberFormatException e) {
					iexcept = true;
				}
				
				if(dexcept && iexcept) {
					//TODO: THROW ERROR EXECEPTION
				}
				
				
				
				//Integer number_value = Integer.valueOf(a.getValue());
				
				if (mlt.matches()) {
					if(compare_int_double(a.getValue(), mlt.group(1), null, is_integer, 0)) {
						transform.add(comparison);
						break;
					}
//					if (number_value < Integer.valueOf(mlt.group(1))) {
//						transform.add(comparison);
//						//System.out.println("AÃ±adiendo < que");
//						break;
//					}
				} else if (mlet.matches()) {
					if(compare_int_double(a.getValue(), mlet.group(1), null, is_integer, 1)) {
						transform.add(comparison);
						break;
					}
//					if (number_value <= Integer.valueOf(mlet.group(1))) {
//						transform.add(comparison);
//						//System.out.println(number_value + "AÃ±adiendo <= que");
//						break;
//					}
				} else if (mbt.matches()) {
					if(compare_int_double(a.getValue(), mbt.group(1), null, is_integer, 2)) {
						transform.add(comparison);
						break;
					}
//					if (number_value > Integer.valueOf(mbt.group(1))) {
//						transform.add(comparison);
//						//System.out.println(number_value + "AÃ±adiendo > que");
//						break;
//					}
				} else if (mbet.matches()) {
					if(compare_int_double(a.getValue(), mbet.group(1), null, is_integer, 3)) {
						transform.add(comparison);
						break;
					}
//					if (number_value >= Integer.valueOf(mbet.group(1))) {
//						transform.add(comparison);
//						//System.out.println(number_value + "AÃ±adiendo >= que");
//						break;
//					}
				} else {
					// interval
					mi.matches();
					System.out.println(mi.group(1) + " - " + mi.group(2) + " - value : " + a.getValue());
					if(compare_int_double(a.getValue(), mi.group(1), mi.group(2), is_integer, 4)) {
						transform.add(comparison);
						break;
					}
//					if (number_value >= Integer.valueOf(mi.group(1)) && number_value <= Integer.valueOf(mi.group(2))) {
//						transform.add(comparison);
//						//System.out.println(number_value + "AÃ±adiendo intervalo " + comparison);
//						break;
//					}
				}
			}
		}
		String[] t = transform.toArray(new String[0]);
		for (String s : t) {
			//System.out.println("Add -> " + s);
		}

		return t;
	}
	
	
	private void setNewValueObjects(ARXResult result, DefaultData data, Event event, String[] att_names_row) {
		if(result == null) {
			//System.out.println(data.toString());
			//System.out.println(data.getDefinition().toString());
			Iterator<String[]> iterator = data.getHandle().iterator();
			iterator.next();
			while(iterator.hasNext()) {
				String[] row = iterator.next();
				for(String r : row) {
					//System.out.println(r);
				}
				String uuid = row[0];
				Object object = event.getObjectByUuid(uuid);
				// replace values in object retrieved
				// att names row has the name of the attributes in order
				for (int i = 1; i < row.length; i++) {	
					// we set the attribute value
					// be aware that in case, that object doesnt have a value
					object.setAttributeValue(att_names_row[i], row[i]); // this method throws false if
					// couldnt be possible
				}
			}
			
			return;
		}
		try {
			// the execution throws null of no solution has been found or a HashMap with
			// values
			HashMap<Integer, String[]> results = printResult(result, data);
			// set objets to event
			results.forEach((index, list) -> {
				String uuid = results.get(index)[0];
				Object object = event.getObjectByUuid(uuid);
				// replace values in object retrieved
				// att names row has the name of the attributes in order
				for (int i = 1; i < list.length; i++) {
					// we set the attribute value
					// be aware that in case, that object doesnt have a value
					object.setAttributeValue(att_names_row[i], list[i]); // this method throws false if
					// couldnt be possible
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private void setNewValuesAttributes(ARXResult result, DefaultData data, Event event) {
		// the execution throws null of no solution has been found or a HashMap with
		// values
		try {
			HashMap<Integer, String[]> results = printResult(result, data);
			results.forEach((i, r) -> {
				//System.out.println("I " + i);
				for (String s : r) {
					//System.out.println("S " + r);
				}
			});
			// set objets to event
			// ______> setnewvaluesattributes

			results.forEach((index, list) -> {
				String uuid = results.get(index)[0];

				Attribute a = event.getAttributeByUuid(uuid);
				// set value in attribute
				a.setValue(results.get(index)[1]);
				//System.out.println("Seteamos atributo " + a.getUuid() + " a " + results.get(index)[1]);
				//System.out.println("Valor en evento " + event.getAttributeByUuid(uuid).getValue());

				// replace values in object retrieved
				// att names row has the name of the attributes in order
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void setStaticHierarchies(models.Policies.Hierarchy hier, String k, ArrayList<String> quasi_and_hierachical,
			HashMap<String, DefaultHierarchy> att_hierarchy) {
		// set static hierarchies
		for (Hierarchy_Object ho : hier.getHierarchyObjects()) {
			if (k.equals(ho.getMisp_object_template())) {
				for (Att_indv ai : ho.getAttributeHierarchies()) {
					if (quasi_and_hierachical.contains(ai.getAttributeName())
							&& ai.getAttributeType().equals("static")) { // if is quasi indicated in the policy
						for (Att_gn g : ai.getAttributeGeneralization()) {
							att_hierarchy.get(ai.getAttributeName()).add(g.getGeneralization().toArray(new String[0]));
							// hierarchy.add(g.getGeneralization().toArray(new String[0]));
						}
					}
				}
				break; // TODO: comprobar el funcionamiento
			}
		}
	}
	
	private void setDinamicHierarchies(Event event, String k, ArrayList<String> v, Hierarchy_Object ho,
			ArrayList<String> quasi_and_hierachical, HashMap<String, String> atts_quasi,
			HashMap<String, DefaultHierarchy> att_hierarchy, DefaultData data) {
		// jerarquÃ­a aÃ±adida
		// recorremos evento para recoger objetos de ese tipo
		for (Object o : event.getObject()) {
			if (o.getName().equals(k)) {
				String[] value_elements = new String[v.size() + 1]; // becacuse of uuid added in dataset
				// get or create uuid for identifiying a row
				String uuid = o.getUuid();
				if (uuid == null || uuid.equals("")) {
					uuid = java.util.UUID.randomUUID().toString();
					o.setUuid(uuid);
				}
				value_elements[0] = uuid;

				for (Attribute a : o.getAttribute()) {
					// for(String s : v ) {
					// System.out.println("V " + s);
					// }
					//System.out.println("Object relation " + a.getObject_relation() + v.indexOf(a.getObject_relation()));
					value_elements[v.indexOf(a.getObject_relation()) + 1] = a.getValue(); // beacuse of uuid added in
					// dataset
					if (quasi_and_hierachical.contains(a.getObject_relation())
							&& atts_quasi.get(a.getObject_relation()).equals("regex")) {
						Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
						// TODO
						String[] t = generate_regex_hierarchy(ai, a);
						// hierarchy.add(t);
						att_hierarchy.get(a.getObject_relation()).add(t);
						// me he quedado aqui
					} else if (quasi_and_hierachical.contains(a.getObject_relation())
							&& atts_quasi.get(a.getObject_relation()).equals("interval")) {
						Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
						String[] t = generate_interval_hierarchy(ai, a);
						// hierarchy.add(t);
						att_hierarchy.get(a.getObject_relation()).add(t);
					}
				}
				data.add(value_elements);
			}
		}
	}
	
	protected static HashMap<Integer, String[]> printResult(final ARXResult result, final Data data) {

		// Print time
		final DecimalFormat df1 = new DecimalFormat("#####0.00");
		final String sTotal = df1.format(result.getTime() / 1000d) + "s";
		System.out.println(" - Time needed: " + sTotal);

		// Extract
		final ARXNode optimum = result.getGlobalOptimum();
		final List<String> qis = new ArrayList<String>(data.getDefinition().getQuasiIdentifyingAttributes());

		if (optimum == null) {
			System.out.println(" - No solution found!");
			return null;
		}

		// Initialize
		final StringBuffer[] identifiers = new StringBuffer[qis.size()];
		final StringBuffer[] generalizations = new StringBuffer[qis.size()];
		int lengthI = 0;
		int lengthG = 0;
		for (int i = 0; i < qis.size(); i++) {
			identifiers[i] = new StringBuffer();
			generalizations[i] = new StringBuffer();
			identifiers[i].append(qis.get(i));
			generalizations[i].append(optimum.getGeneralization(qis.get(i)));
			if (data.getDefinition().isHierarchyAvailable(qis.get(i)))
				generalizations[i].append("/").append(data.getDefinition().getHierarchy(qis.get(i))[0].length - 1);
			lengthI = Math.max(lengthI, identifiers[i].length());
			lengthG = Math.max(lengthG, generalizations[i].length());
		}

		// Padding
		for (int i = 0; i < qis.size(); i++) {
			while (identifiers[i].length() < lengthI) {
				identifiers[i].append(" ");
			}
			while (generalizations[i].length() < lengthG) {
				generalizations[i].insert(0, " ");
			}
		}

		// Print
		System.out.println(" - Information loss: " + result.getGlobalOptimum().getLowestScore() + " / "
				+ result.getGlobalOptimum().getHighestScore());
		System.out.println(" - Optimal generalization");
		for (int i = 0; i < qis.size(); i++) {
			System.out.println("   * " + identifiers[i] + ": " + generalizations[i]);
		}
		System.out.println(" - Statistics");
		System.out.println(
				result.getOutput(result.getGlobalOptimum(), false).getStatistics().getEquivalenceClassStatistics());

		Iterator<String[]> transformed = result.getOutput(false).iterator();
		transformed.next();
		HashMap<Integer, String[]> map = new HashMap<Integer, String[]>();
		Integer i = 0;
		while (transformed.hasNext()) {
			System.out.println(" ");
			map.put(i, transformed.next());
			i++;
		}
		HashMap<Integer, String[]> results = new HashMap<Integer, String[]>();
		for (int z = 0; z < map.size(); z++) {
			System.out.println("z " + z);
			String[] array = map.get(z);
			results.put(z, array);
			System.out.println(Arrays.toString(array));
		}
		return results;
	}
}
