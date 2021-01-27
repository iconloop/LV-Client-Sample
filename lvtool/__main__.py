from lvtool.handlers import handlers
from lvtool.parsers import init_parsers


def main(argv=None):
    """lv-tool's main

    :param argv: If None, parse_args uses sys.argv [1 :].
    :return:
    """
    parser = init_parsers()
    args = parser.parse_args(argv)

    return handlers[args.dest](args)
