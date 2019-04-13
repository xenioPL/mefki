import datetime

from flask import Flask, request
import requests
import csv
import json
from io import StringIO

app = Flask(__name__)
map = {}

@app.route("/doMath")
def doMath():
    raw_csv = requests.get(
        "http://rowermevo.pl/maps/locations.txt?key=jF5puHQe3CqssPZh",
        headers={
            "User-Agent": "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
        },
    ).text
    f = StringIO(raw_csv)
    reader = csv.DictReader(f)
    for row in reader:
        if row["NUMER STACJI"] not in map.keys():
            map[row["NUMER STACJI"]] = {"time": None, "average": 0, "total_minutes": 0}
        if (
            row["DOSTĘPNE ROWERY"] != "0"
            and map[row["NUMER STACJI"]]["time"] is not None
        ):
            if map[row["NUMER STACJI"]]["average"] != 0:
                map[row["NUMER STACJI"]]["average"] = (
                    map[row["NUMER STACJI"]]["total_minutes"]
                    + (
                        (
                            datetime.datetime.now() - map[row["NUMER STACJI"]]["time"]
                        ).total_seconds()
                        / 60
                    )
                ) / (
                    (
                        map[row["NUMER STACJI"]]["total_minutes"]
                        / map[row["NUMER STACJI"]]["average"]
                    )
                    + 1
                )
                map[row["NUMER STACJI"]]["total_minutes"] += (
                    datetime.datetime.now() - map[row["NUMER STACJI"]]["time"]
                ).total_seconds() / 60
                map[row["NUMER STACJI"]]["time"] = None
            else:
                map[row["NUMER STACJI"]]["average"] = (
                    datetime.datetime.now() - map[row["NUMER STACJI"]]["time"]
                ).total_seconds() / 60
                map[row["NUMER STACJI"]]["total_minutes"] = (
                    datetime.datetime.now() - map[row["NUMER STACJI"]]["time"]
                ).total_seconds() / 60
                map[row["NUMER STACJI"]]["time"] = None
        if row["DOSTĘPNE ROWERY"] == "0" and map[row["NUMER STACJI"]]["time"] is None:
            map[row["NUMER STACJI"]]["time"] = datetime.datetime.now()

    print(map)
    return "lol"


@app.route("/getStations")
def getStations():
    raw_csv = requests.get(
        "http://rowermevo.pl/maps/locations.txt?key=jF5puHQe3CqssPZh",
        headers={
            "User-Agent": "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
        },
    ).text
    f = StringIO(raw_csv)
    reader = csv.DictReader(f)
    json_out = []
    for row in reader:
        json_out.append(
            {
                "id": int(row["NUMER STACJI"]),
                "lat": float(row["WSPÓŁRZĘDNE"].split(",")[0]),
                "lon": float(row["WSPÓŁRZĘDNE"].split(",")[1]),
            }
        )
    return json.dumps(json_out)


@app.route("/getNumberFromStations")
def getNumberFromStations():
    raw_csv = requests.get(
        "http://rowermevo.pl/maps/locations.txt?key=jF5puHQe3CqssPZh",
        headers={
            "User-Agent": "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
        },
    ).text
    f = StringIO(raw_csv)
    reader = csv.DictReader(f)
    json_out = []
    stations = set([])
    for station in request.args.get("stations").split(","):
        stations.add(station)

    for row in reader:
        if row["NUMER STACJI"] in stations:
            json_out.append(
                {"id": int(row["NUMER STACJI"]), "number": int(row["DOSTĘPNE ROWERY"])}
            )
    return json.dumps(json_out)



if __name__ == "__main__":
    app.run()
