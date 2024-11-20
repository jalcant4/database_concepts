-- p1.3_jalcant4
DROP TABLE Measurement;
DROP TABLE PhoneNumber;
DROP TABLE Employee;
DROP TABLE Room;
DROP TABLE Department;
DROP TABLE Building;

-- Create new tables
CREATE TABLE Department (
    DeptID INT PRIMARY KEY,
    DeptName VARCHAR(50),
    StreetAddress VARCHAR(50),
    City VARCHAR(50),
    State VARCHAR(2),
    Zip INT,
    WebAddress VARCHAR(50),
    EmailAddress VARCHAR(50),
    Phone VARCHAR(15)
);

CREATE TABLE Building (
    BuildingName VARCHAR(50) PRIMARY KEY,
    NumberOfFloors INT,
    NumberOfRooms INT
);

CREATE TABLE Room (
    BuildingName VARCHAR(50),
    RoomNumber INT,
    Area INT,
    ConferenceFlag INT,
    OfficeFlag INT,
    LabFlag INT,
    PRIMARY KEY (BuildingName, RoomNumber),
    FOREIGN KEY (BuildingName) REFERENCES Building(BuildingName)
);

CREATE TABLE Employee (
    EmpID INT PRIMARY KEY,
    YOB DATE,
    EmailAddress VARCHAR(50),
    DeptID INT,
    BuildingName VARCHAR(50),
    RoomNumber INT,
    FOREIGN KEY (DeptID) REFERENCES Department(DeptID),
    FOREIGN KEY (BuildingName, RoomNumber) REFERENCES Room(BuildingName, RoomNumber)
);

CREATE TABLE PhoneNumber (
    BuildingName VARCHAR(50),
    RoomNumber INT,
    Phone VARCHAR(15),
    PRIMARY KEY (BuildingName, RoomNumber, Phone),
    FOREIGN KEY (BuildingName, RoomNumber) REFERENCES Room(BuildingName, RoomNumber)
);

CREATE TABLE Measurement (
    BuildingName VARCHAR(50),
    RoomNumber INT,
    DateTime TIMESTAMP,
    Sound INT,
    Temperature INT,
    Light INT,
    PRIMARY KEY (BuildingName, RoomNumber, DateTime),
    FOREIGN KEY (BuildingName, RoomNumber) REFERENCES Room(BuildingName, RoomNumber)
);

INSERT INTO Department (DeptID, DeptName, StreetAddress, City, State, Zip, WebAddress, EmailAddress, Phone)
VALUES (1, 'HR', '123 Main St', 'Cityville', 'CA', 12345, 'www.hrdept.com', 'hr@example.com', '123-456-7890');

INSERT INTO Department (DeptID, DeptName, StreetAddress, City, State, Zip, WebAddress, EmailAddress, Phone)
VALUES (2, 'IT', '456 Tech Rd', 'Techland', 'CA', 54321, 'www.itdept.com', 'it@example.com', '987-654-3210');

INSERT INTO Building (BuildingName, NumberOfFloors, NumberOfRooms)
VALUES ('Building A', 5, 50);

INSERT INTO Building (BuildingName, NumberOfFloors, NumberOfRooms)
VALUES ('Building B', 7, 75);

INSERT INTO Room (BuildingName, RoomNumber, Area, ConferenceFlag, OfficeFlag, LabFlag)
VALUES ('Building A', 101, 200, 1, 0, 0);

INSERT INTO Room (BuildingName, RoomNumber, Area, ConferenceFlag, OfficeFlag, LabFlag)
VALUES ('Building A', 102, 150, 0, 1, 1);

INSERT INTO Room (BuildingName, RoomNumber, Area, ConferenceFlag, OfficeFlag, LabFlag)
VALUES ('Building B', 201, 300, 1, 0, 0);

INSERT INTO Employee (EmpID, YOB, EmailAddress, DeptID, BuildingName, RoomNumber)
VALUES (1, TO_DATE('1990-05-15', 'YYYY-MM-DD'), 'employee1@example.com', 1, 'Building A', 101);

INSERT INTO Employee (EmpID, YOB, EmailAddress, DeptID, BuildingName, RoomNumber)
VALUES (2, TO_DATE('1985-10-20', 'YYYY-MM-DD'), 'employee2@example.com', 2, 'Building B', 201);

INSERT INTO PhoneNumber (BuildingName, RoomNumber, Phone)
VALUES ('Building A', 101, '123-111-1111');

INSERT INTO PhoneNumber (BuildingName, RoomNumber, Phone)
VALUES ('Building B', 201, '456-222-2222');

INSERT INTO Measurement (BuildingName, RoomNumber, DateTime, Sound, Temperature, Light)
VALUES ('Building A', 101, TIMESTAMP '2023-10-26 08:00:00', 50, 22, 500);

INSERT INTO Measurement (BuildingName, RoomNumber, DateTime, Sound, Temperature, Light)
VALUES ('Building B', 201, TIMESTAMP '2023-10-26 09:30:00', 40, 20, 600);

SELECT d.Phone, d.DeptName, d.StreetAddress
FROM Department d;

SELECT b.BuildingName
FROM Building b
WHERE b.NumberOfFloors < 4;

SELECT BuildingName, RoomNumber
FROM Room
WHERE OfficeFlag = 1 AND LabFlag = 1;

SELECT b.BuildingName, COUNT(r.RoomNumber) AS NumberOfRooms
FROM Building b
LEFT JOIN Room r ON b.BuildingName = r.BuildingName
GROUP BY b.BuildingName;

SELECT BuildingName, RoomNumber, AVG(Temperature) AS AvgTemperature
FROM Measurement
GROUP BY BuildingName, RoomNumber;

SELECT e.EmpID
FROM Employee e
GROUP BY e.EmpID
HAVING COUNT(EmailAddress) = 2;

SELECT r.BuildingName, r.RoomNumber, r.Area
FROM Room r
WHERE (r.BuildingName, r.RoomNumber) NOT IN (
    SELECT m.BuildingName, m.RoomNumber
    FROM Measurement m
);

SELECT p.Phone
FROM PhoneNumber p
WHERE (p.BuildingName, p.RoomNumber) IN (
    SELECT r.BuildingName, r.RoomNumber
    FROM (
        SELECT BuildingName, RoomNumber, AVG(Temperature) AS AvgTemp
        FROM Measurement
        GROUP BY BuildingName, RoomNumber, TRUNC(DateTime)
    ) r
    WHERE AvgTemp = (SELECT MAX(AvgTemp) FROM (
        SELECT BuildingName, RoomNumber, AVG(Temperature) AS AvgTemp
        FROM Measurement
        GROUP BY BuildingName, RoomNumber, TRUNC(DateTime)
    ))
);