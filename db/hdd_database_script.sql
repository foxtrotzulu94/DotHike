--
-- File generated with SQLiteStudio v3.0.6 on Sun Nov 1 01:21:50 2015
--
-- Text encoding used: UTF-8
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Table: humidity
CREATE TABLE humidity (id INTEGER PRIMARY KEY AUTOINCREMENT, hike_id INTEGER REFERENCES hikes (id) NOT NULL, min REAL NOT NULL, avg REAL NOT NULL, max REAL NOT NULL)

-- Table: temperature
CREATE TABLE temperature (id INTEGER PRIMARY KEY AUTOINCREMENT, hike_id INTEGER REFERENCES hikes (id) NOT NULL, min REAL NOT NULL, avg REAL NOT NULL, max REAL NOT NULL)

-- Table: hike_coordinates
CREATE TABLE hike_coordinates (id INTEGER PRIMARY KEY AUTOINCREMENT, hike_id INTEGER REFERENCES hikes (id) NOT NULL, longitude REAL NOT NULL, latitude REAL NOT NULL, altitude REAL)

-- Table: hikes
CREATE TABLE hikes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, startTime TIME NOT NULL, endTime TIME NOT NULL)

-- Table: steps
CREATE TABLE steps (id INTEGER PRIMARY KEY AUTOINCREMENT, hike_id INTEGER REFERENCES hikes (id) NOT NULL, count INTEGER)

-- Table: pressure
CREATE TABLE pressure (id INTEGER PRIMARY KEY AUTOINCREMENT, hike_id INTEGER REFERENCES hikes (id) NOT NULL, min REAL NOT NULL, avg REAL NOT NULL, max REAL NOT NULL)

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
