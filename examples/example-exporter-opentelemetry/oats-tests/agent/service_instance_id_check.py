#!/usr/bin/env python3
"""
Check if the service instance id is present in the exported data.
Returns 0 if the service instance id is present in the exported data.
"""
import json
import urllib.parse
from urllib.request import urlopen


def get_json(url):
    with urlopen(url) as response:
        return json.loads(response.read().decode("utf-8"))


def main():
    # Query Prometheus for target_info
    res = get_json("http://localhost:9090/api/v1/query?query=target_info")

    # Uncomment for local debugging
    # with open('example_target_info.json') as f:
    #     res = json.load(f)

    instance_ids = {
        r["metric"]["instance"]
        for r in res["data"]["result"]
        if r["metric"].get("service_name") != "otelcol-contrib"
    }
    instance_ids = list(instance_ids)

    print(f"Instance ids found:{instance_ids}")
    if len(instance_ids) > 1:
        print("More than one instance id found")
        print(res)

    # Both the agent and the exporter should report the same instance id
    assert len(instance_ids) == 1, "Expected exactly one instance id"

    query = f'target_info{{instance="{instance_ids[0]}"}}'
    encoded_query = urllib.parse.quote_plus(query)
    res = get_json(f"http://localhost:9090/api/v1/query?query={encoded_query}")

    infos = res["data"]["result"]
    print(infos)

    # They should not have the same target info (e.g. only the agent has telemetry_distro_name)
    assert len(infos) == 2, "Expected two target info results"


if __name__ == "__main__":
    main()
