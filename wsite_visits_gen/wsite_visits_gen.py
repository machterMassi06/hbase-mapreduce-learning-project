import csv
import random
from datetime import datetime, timedelta

# Reproductibilite
random.seed(42)

# Some of simples globales var 

N_REC = 10_000 # number of records 

fields = [
    "visit_id",
    "user_id",
    "page",
    "country",
    "timestamp"
]

countries = ["FR", "UK", "ES", "USA", "BE", "DZ"]

pages = [
    "/",
    "/home",
    "/help",
    "/about",
    "/products",
    "/pricing",
    "/contact",
    "/hr",
    "/blog"
]

start_date = datetime(2025, 1, 1)

with open("visits.csv", "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(fields)

    for visit_id in range(1, N_REC + 1):

        user_id = f"{random.randint(1, 500):04d}"

        page = random.choice(pages)

        country = random.choice(countries)

        timestamp = (
            start_date +
            timedelta(
                days=random.randint(0, 364),
                seconds=random.randint(0, 86399)
            )
        )

        v_id = f"{visit_id:0{len(str(N_REC))}d}" # id from 00..01 to N_REC

        writer.writerow([
            v_id,
            user_id,
            page,
            country,
            timestamp.isoformat()
        ])

print(f"{N_REC} records generated.")