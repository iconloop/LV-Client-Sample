import json
from typing import List


def read_input_file(path) -> dict:
    with open(path, "r") as f:
        return json.load(f)


def read_clue_file(path) -> List[str]:
    with open(path, "r") as f:
        return [line.strip("\n") for line in f.readlines()]
