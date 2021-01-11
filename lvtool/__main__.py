from .handlers import handlers
from .parsers import init_parsers


def main():
    parser = init_parsers()
    args = parser.parse_args()

    return handlers[args.dest](args)
