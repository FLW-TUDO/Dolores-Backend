package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class Employee(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    var id: ObjectId = ObjectId.get(),
    val name: String,
    val gender: Boolean,
    var employmentRound: Int,
    val age: Int,
    var contractType: Int,
    var endRound: Int
)

class EmployeeDynamic(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    var id: ObjectId = ObjectId.get(),
    val employee: Employee,
    var qualification: Int,
    @JsonSerialize(using = ToStringSerializer::class)
    var process: Process,
    var motivation: Int,
    var salary: Double,
    var qmRound: Int,
    var fpRound: Int,
    var secRound: Int
)

data class EmployeeHireMessage(
    val employeeId: ObjectId,
    val process: Int,
    val contractType: Int
)

data class EmployeeTrainingMessage(
    val employeeId: ObjectId,
    val qualification: Int
)

data class EmployeeProcessMessage(
    val employeeId: ObjectId,
    val process: Int
)

val employee_last_names: List<String> = listOf(
    "Schmidt",
    "Müller",
    "Schmitz",
    "Neumann",
    "Schneider",
    "Fischer",
    "Schulz",
    "Graf",
    "Klein",
    "Koch",
    "Weber",
    "Schmitt",
    "Schäfer",
    "Lange",
    "Fuchs",
    "Meyer",
    "Becker",
    "Krüger",
    "Lehmann",
    "Bergmann",
    "Hartmann",
    "Weichelt",
    "Tkocz",
    "Peters",
    "Wolf",
    "Franz",
    "Baumann",
    "Schwarz",
    "Friese",
    "Schröder",
    "Vitting",
    "Oppermann",
    "Jacobi",
    "Schwenke",
    "Nakaten",
    "Witte",
    "Janssen",
    "Melzer",
    "Jerke",
    "Meier",
    "Braun",
    "Richter",
    "Garcia",
    "Hegermann",
    "Blesing",
    "Ruditis",
    "Roters",
    "Lennartz",
    "Köhler",
    "Krieger",
    "Dörre",
    "Wuttke",
    "Vogel",
    "Arndt",
    "Albers",
    "Miller",
    "Meissner",
    "Hermanns",
    "Schröter",
    "Rumpf",
    "Herzog",
    "Jansen",
    "König",
    "Bardts",
    "Winter",
    "Bartsch",
    "Berger",
    "Weiss",
    "Hoffmann",
    "Bauer",
    "Krause",
    "Werner",
    "Schulze",
    "Kaiser",
    "Jachmann",
    "Sachtje",
    "Münch",
    "Wulfert",
    "Wurst",
    "Kraft",
    "Berg",
    "Neuhaus",
    "Pauli",
    "Rose",
    "Schlüter",
    "Kiefer",
    "Kailus",
    "Mentel",
    "Merk",
    "Schäfers",
    "Yilmaz",
    "Witt",
    "Quadflieg",
    "Stolten",
    "Behrens",
    "Beckmann",
    "Göbel",
    "Dohmen",
    "Funke",
    "Bock",
    "Krausen",
    "Sommer",
    "Reuter",
    "Rupp",
    "Rösener",
    "Krings",
    "Herrmann",
    "Klaus",
    "Dieners",
    "Hennemann",
    "Lux",
    "Schrumpf",
    "Wiemers",
    "Heinrich",
    "Fastabend",
    "Glöde",
    "Knevels",
    "Buchholz",
    "Barczi",
    "Wolfgarten",
    "Dohrmann",
    "Wagner",
    "Mayer",
    "Möller"
)

val employee_female_names: List<String> = listOf(
    "Mia",
    "Hanna",
    "Leonie",
    "Lea",
    "Lena",
    "Anna",
    "Emma",
    "Emilie",
    "Marie",
    "Lili",
    "Sarah",
    "Lara",
    "Laura",
    "Sophie",
    "Sophia",
    "Lina",
    "Nele",
    "Johanna",
    "Maja",
    "Alina",
    "Julia",
    "Clara",
    "Emilia",
    "Leni",
    "Lisa",
    "Zoe",
    "Luisa",
    "Paula",
    "Jana",
    "Jasmin",
    "Lucy",
    "Pia",
    "Melina",
    "Finja",
    "Josefine",
    "Amy",
    "Annika",
    "Emelie",
    "Stella",
    "Angelina",
    "Lia",
    "Chiara",
    "Fiona",
    "Ida",
    "Antonia",
    "Celina",
    "Jule",
    "Matilda",
    "Helena",
    "Nina",
    "Vanessa",
    "Isabel",
    "Marlene",
    "Celine",
    "Maria",
    "Greta",
    "Pauline",
    "Selina",
    "Lotta",
    "Carolin",
    "Ronja",
    "Mara",
    "Melissa",
    "Jolina",
    "Luise",
    "Eva",
    "Theresa",
    "Vivien",
    "Elisa",
    "Merle",
    "Aylin",
    "Frieda",
    "Jette",
    "Lana",
    "Luna",
    "Sina",
    "Carla",
    "Helene",
    "Kim",
    "Larissa",
    "Nora",
    "Romy",
    "Samira",
    "Nelli",
    "Ella",
    "Mira",
    "Alicia",
    "Linda",
    "Elena",
    "Milena",
    "Miriam"
)

val employee_male_names: List<String> = listOf(
    "Julian",
    "Philip",
    "Elias",
    "Niklas",
    "Noah",
    "Jan",
    "Moritz",
    "Jannik",
    "Tom",
    "Nico",
    "Simon",
    "Alexander",
    "Fabian",
    "David",
    "Eric",
    "Jacob",
    "Florian",
    "Nils",
    "Lennard",
    "Nick",
    "Lenny",
    "Linus",
    "Mika",
    "Jason",
    "Colin",
    "Henri",
    "Justin",
    "Johannes",
    "Anton",
    "Rafael",
    "Sebastian",
    "Tobias",
    "Dominic",
    "Daniel",
    "Lennox",
    "Jonathan",
    "Hannes",
    "Jannis",
    "Julius",
    "Marlon",
    "Vincent",
    "Emil",
    "Benjamin",
    "Joel",
    "Timo",
    "Adrian",
    "Robin",
    "Till",
    "Leonard",
    "Aaron",
    "Marvin",
    "Leo",
    "Carl",
    "Jona",
    "Oscar",
    "Samuel",
    "Joshua",
    "Jamie",
    "Kevin",
    "Matthis",
    "Marc",
    "Ole",
    "Lasse",
    "Kilian",
    "Silas",
    "John",
    "Justus",
    "Oliver",
    "Phil",
    "Dennis",
    "Jeremy",
    "Johann",
    "Gabriel",
    "Liam",
    "Levin",
    "Theo",
    "Matteo",
    "Tyler",
    "Lars",
    "Pascal",
    "Bastian",
    "Michael",
    "Bennet",
    "Marcel",
    "Malte"
)