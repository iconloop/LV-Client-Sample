from setuptools import setup, find_packages

req_tests = ["pytest"]
req_lint = ["flake8", "flake8-docstrings"]
req_dev = req_tests + req_lint

setup_options = {
    "name": "TestTool",
    "version": "0.0.1",
    "description": "TestClient for Lite Vault",
    "packages": find_packages(),
    "python_requires": ">=3.9",
    "install_requires": [
        "requests",
        "Assam @ git+ssh://git@github.com/icon-project/Assam.git",  # noqa: E501
    ],
    "extras_require": {
        "tests": req_tests,
        "lint": req_lint,
        "dev": req_dev
    },
    "package_dir": {"": "."},
    "entry_points": {
        "console_scripts": [
            "lv-tool=lvtool.__main__:main"
        ]
    }
}

setup(**setup_options)
