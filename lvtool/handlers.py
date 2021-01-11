import json
from typing import List

from .helper import read_input_file, read_clue_file
from .interfaces import Manager, Storage
from .types import Commands


def _handle_get_storages(args):
    output_path = args.output

    manager = Manager(endpoint=args.endpoint)
    # backup_response: dict = manager.request_vpr()  # TODO: THIS should contain VPRequest.
    vid_response = manager.issue_vid_request()  # TODO: Fill VID Request according to responded VPR!

    with open(output_path, "w") as f:
        f.write(json.dumps(vid_response, indent=4))


def _handle_token(args):
    vid_response_msg: dict = read_input_file(args.input)
    storages: List[dict] = []

    for storage_info in vid_response_msg["storages"]:
        storage = Storage(storage_info)
        storage.token_request()
        storages.append(storage.to_json())

    with open(args.output, "w") as f:
        f.write(json.dumps({
            "vID": vid_response_msg["vID"],
            "recovery_key": vid_response_msg["recovery_key"],
            "storages": storages
        }, indent=4))


def _handle_store(args):
    clues: List[str] = read_clue_file(args.clues)
    vid_response_msg: dict = read_input_file(args.input)
    storages: List[dict] = []

    for clue, storage_info in zip(clues, vid_response_msg["storages"]):
        storage = Storage(storage_info)
        storage.store_request(clue)
        storages.append(storage.to_json())

    with open(args.output, "w") as f:
        f.write(json.dumps({
            "vID": vid_response_msg["vID"],
            "recovery_key": vid_response_msg["recovery_key"],
            "storages": storages
        }, indent=4))


def _handle_read(args):
    vid_response_msg: dict = read_input_file(args.input)
    storages = (Storage(storage_info) for storage_info in vid_response_msg["storages"])
    gathered_clues = []

    for storage in storages:
        response = storage.clue_request()
        gathered_clues.append(response["clue"])

    with open(args.output, "w") as f:
        f.writelines("\n".join(gathered_clues))


handlers = {
    Commands.VPR: _handle_get_storages,
    Commands.TOKEN: _handle_token,
    Commands.STORE: _handle_store,
    Commands.RESTORE: _handle_read,
}
