#!/bin/bash


tmux kill-session -t mysessionAPI
cd ChainREST/
npm install
tmux new   -s mysessionAPI -d ' DEBUG=chainapi:* npm start'
tmux a
