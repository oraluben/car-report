--
-- Update data to fit with the new constraints (every refueling has to have a fuel type).
--
INSERT INTO fueltypes (name, tank, cars_id)
	SELECT 'Default', 1, cars._id
	FROM cars
	LEFT JOIN fueltypes ON fueltypes.cars_id = cars._id
	WHERE fueltypes._id IS NULL;
UPDATE refuelings
	SET fueltypes_id = (SELECT fueltypes._id FROM fueltypes WHERE fueltypes.cars_id = refuelings.cars_id LIMIT 1)
	WHERE fueltypes_id IS NULL;

--
-- Update schema.
--
CREATE TABLE cars2 (
	Id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL ON CONFLICT FAIL,
	color INTEGER NOT NULL ON CONFLICT FAIL,
	suspended_since INTEGER);
INSERT INTO cars2 (Id, name, color, suspended_since)
	SELECT _id, name, color, suspended_since
	FROM cars;
DROP TABLE cars;
ALTER TABLE cars2 RENAME TO cars;

CREATE TABLE fuel_tanks (
	Id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL ON CONFLICT FAIL,
	car INTEGER NOT NULL ON CONFLICT FAIL REFERENCES cars(Id) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE fuel_types (
	Id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL ON CONFLICT FAIL UNIQUE ON CONFLICT FAIL);
CREATE TABLE fuel_types_fuel_tanks (
	Id INTEGER PRIMARY KEY AUTOINCREMENT,
	fuel_tank INTEGER NOT NULL ON CONFLICT FAIL REFERENCES fuel_tanks(Id) ON DELETE CASCADE ON UPDATE CASCADE,
	fuel_type INTEGER NOT NULL ON CONFLICT FAIL REFERENCES fuel_types(Id) ON DELETE CASCADE ON UPDATE CASCADE);
INSERT INTO fuel_types (name)
	SELECT DISTINCT name
	FROM fueltypes;
INSERT INTO fuel_tanks (name, car)
	SELECT DISTINCT tank, cars_id
	FROM fueltypes;
INSERT INTO fuel_types_fuel_tanks (fuel_tank, fuel_type)
	SELECT fuel_tanks.Id, fuel_types.Id
	FROM fueltypes
	JOIN fuel_tanks ON fueltypes.cars_id = fuel_tanks.car AND fueltypes.tank = fuel_tanks.name
	JOIN fuel_types ON fueltypes.name = fuel_types.name;

CREATE TABLE other_costs (
	Id INTEGER PRIMARY KEY AUTOINCREMENT,
	title TEXT NOT NULL ON CONFLICT FAIL,
	date INTEGER NOT NULL ON CONFLICT FAIL,
	mileage INTEGER,
	price REAL NOT NULL ON CONFLICT FAIL,
	recurrence TEXT NOT NULL ON CONFLICT FAIL,
	note TEXT NOT NULL ON CONFLICT FAIL,
	car INTEGER NOT NULL ON CONFLICT FAIL REFERENCES cars(Id) ON DELETE CASCADE ON UPDATE CASCADE);
INSERT INTO other_costs (Id, title, date, mileage, price, recurrence, note, car)
	SELECT _id, title, date, tachometer, price,
		CASE WHEN repeat_interval = 0 THEN 'ONCE'
		WHEN repeat_interval = 1 THEN 'DAY'
		WHEN repeat_interval = 2 THEN 'MONTH'
		WHEN repeat_interval = 3 THEN 'QUARTER'
		WHEN repeat_interval = 4 THEN 'YEAR'
		END || ' ' || repeat_multiplier AS recurrence, note, cars_id
	FROM othercosts;
DROP TABLE othercosts;

CREATE TABLE refuelings2 (
	Id INTEGER PRIMARY KEY AUTOINCREMENT,
	date INTEGER NOT NULL ON CONFLICT FAIL,
	mileage INTEGER NOT NULL ON CONFLICT FAIL,
	volume REAL NOT NULL ON CONFLICT FAIL,
	price REAL NOT NULL ON CONFLICT FAIL,
	partial INTEGER NOT NULL ON CONFLICT FAIL,
	note TEXT NOT NULL ON CONFLICT FAIL,
	fuel_tank INTEGER NOT NULL ON CONFLICT FAIL REFERENCES fuel_tanks(Id) ON DELETE CASCADE ON UPDATE CASCADE,
	fuel_type INTEGER NOT NULL ON CONFLICT FAIL REFERENCES fuel_types(Id) ON DELETE CASCADE ON UPDATE CASCADE);
INSERT INTO refuelings2 (Id, date, mileage, volume, price, partial, note, fuel_tank, fuel_type)
	SELECT refuelings._id, date, tachometer, volume, price, partial, note, fuel_tanks.Id, fuel_types.Id
	FROM refuelings
	JOIN fueltypes ON refuelings.fueltypes_id = fueltypes._id
	JOIN fuel_tanks ON fueltypes.cars_id = fuel_tanks.car AND fueltypes.tank = fuel_tanks.name
	JOIN fuel_types ON fueltypes.name = fuel_types.name;
DROP TABLE fueltypes;
DROP TABLE refuelings;
ALTER TABLE refuelings2 RENAME TO refuelings;