#!/bin/bash
echo "Lanzando blockchain"
set -x
tmux kill-session -t mysessionblockcaion
sh scripts/borrarTodo.sh

tmux new  -s mysessionblockcaion  -n 'orderer'  -d 'docker-compose -f docker-compose-orderer-1.yml up' \;
sleep 2

tmux split-window  -h -d 'docker-compose -f docker-compose-ca-org1.yml up' \;
tmux split-window  -v -d  'docker-compose -f docker-compose-peer0-org1.yml up' \;

sleep 5
docker-compose -f docker-compose-cli-peer0.yml up -d
tmux select-pane -L
tmux split-window -v 'docker exec -it cli bash -c "chmod +x ./scripts/crearCanal.sh; ./scripts/crearCanal.sh"'  \;
tmux select-pane -U
tmux a 

set +x
echo 'docker exec -it cli bash
 sh scripts/installSC.sh NoeliaSC 1'
