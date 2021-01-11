if [[ -z $1 ]]; then
  echo "clue file needed"
  exit
fi

lv-tool vpr -e localhost:8000 -o storages.json
lv-tool token -f storages.json -o token_output.json
lv-tool store $1 -f token_output.json -o store_output.json
lv-tool read -f store_output.json -o restored_clues.txt
