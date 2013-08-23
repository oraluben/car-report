CREATE TABLE refuelings2 (
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	`date` INTEGER NOT NULL,
	tachometer INTEGER NOT NULL,
	volume REAL NOT NULL,
	price REAL NOT NULL,
	`partial` INTEGER NOT NULL,
	note TEXT NOT NULL,
	cars_id INTEGER NOT NULL,
	fueltypes_id INTEGER DEFAULT NULL,
	FOREIGN KEY(cars_id) REFERENCES cars(_id) ON UPDATE CASCADE ON DELETE CASCADE
	FOREIGN KEY(fueltypes_id) REFERENCES fueltypes(_id) ON UPDATE CASCADE ON DELETE SET DEFAULT);
INSERT INTO refuelings2 (_id, `date`, tachometer, volume, price, `partial`, note, cars_id)
	SELECT refuelings._id, `date`, tachometer, volume, price, `partial`, note, cars_id
	FROM refuelings
	JOIN cars ON cars._id = refuelings.cars_id;
DROP TABLE refuelings;
ALTER TABLE refuelings2 RENAME TO refuelings;