"""Guard the release reactor configuration without credentials or publishing artifacts."""

import re
import shlex
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def profiles(text):
    """Read the single explicit Maven profile selection in a release command."""
    selections = re.findall(r"\s-P\s+('[^']*'|\"[^\"]*\"|[^\s]+)", text)
    if len(selections) != 1:
        raise AssertionError("Expected exactly one explicit Maven -P selection")
    return set(shlex.split(selections[0])[0].split(","))


class ReleaseConfigurationTest(unittest.TestCase):
    def test_build_and_deploy_use_the_same_release_reactor(self):
        build = (ROOT / ".mise/tasks/build-release.sh").read_text()
        deploy = (ROOT / ".github/workflows/release.yml").read_text()
        expected = {"release", "!default", "!examples-and-integration-tests"}
        self.assertEqual(profiles(build), expected)
        self.assertEqual(profiles(deploy), profiles(build))

    def test_regression_detects_the_failed_release_configuration(self):
        build = (ROOT / ".mise/tasks/build-release.sh").read_text()
        failed_command = "mvn deploy -P 'release,!default' -Dmaven.test.skip=true"
        self.assertNotEqual(profiles(failed_command), profiles(build))


if __name__ == "__main__":
    unittest.main()
