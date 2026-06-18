#!/usr/bin/env python3
import json
from urllib.request import urlopen


def get_json(url):
    with urlopen(url) as response:
        return json.loads(response.read().decode("utf-8"))


res = get_json("http://localhost:9090/api/v1/query?query=uptime_seconds_total")
results = res["data"]["result"]
assert results, "Expected uptime_seconds_total to be present"
