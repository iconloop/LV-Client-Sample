from lvtool.handlers import Handler
from lvtool.parsers import init_parsers
import sys


def main(argv=None):
    """lv-tool's main

    :param argv: If None, parse_args uses sys.argv [1:].
    :return:
    """
    parser = init_parsers()
    sys.argv = argv if argv else sys.argv
    args = parser.parse_args()

    return Handler()(args.dest, args)
