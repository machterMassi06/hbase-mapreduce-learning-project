import csv
import random
from faker import Faker

# Reproductibilité
random.seed(42)
fake = Faker("fr_FR")
Faker.seed(42)

N_USERS = 500


with open("data/users.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)

    writer.writerow([
        "user_id",
        "first_name",
        "last_name",
        "age",
        "gender"
    ])

    for user_num in range(1, N_USERS + 1):

        user_id = f"{user_num:04d}"

        gender = random.choice(["M", "F"])

        if gender == "M":
            first_name = fake.first_name_male()
        else:
            first_name = fake.first_name_female()

        last_name = fake.last_name()

        age = random.randint(18, 75)


        writer.writerow([
            user_id,
            first_name,
            last_name,
            age,
            gender
        ])

print(f"{N_USERS} users generated.")