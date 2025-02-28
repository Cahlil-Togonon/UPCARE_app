# UPCARE Routing Application

This repository contains all the necessary files to run the UPCARE routing application. This application uses AQI data of UPCARE air quality sensors, performs Kriging interpolation to street-level AQI, saves the data into a PostGIS database. The street-level AQI data is then used by the GraphHopper server to perform AQ-weighted routing. A Leaflet-based web application is also available to show current AQ map and routing results in Metro Manila, Philippines.

## Folder Directory

This repository contains the following:

```
UPCARE_app
├── coe-199_capstone # python data processing backend
│   ├── Dockerfile
│   └── ...
├── express-leaflet # Leaflet web application
│   ├── Dockerfile
│   └── ...
├── routing_backend # GraphHopper router server
│   ├── Dockerfile
│   ├── router_API.yaml # OpenAPI specifications 
│   └── ...
├── docker-compose.yaml
├── manila_osm.sql  # init files for database
├── osm_ids.sql
└── osm_streets.sql
```

## Running the Application

Follow the following steps:

1. Clone this repository and its submodules using this command:

    ```
    git clone --recurse-submodules https://github.com/Cahlil-Togonon/UPCARE_app.git
    ```

2. Before building the application, download the Philippines OSM .pbf data file in the `./UPCARE-app/routing-backend/maps/` folder.

    ```
    cd ./UPCARE-app/routing-backend/maps/

    wget https://download.geofabrik.de/asia/philippines-latest.osm.pbf
    ```

3. Build the application using docker-compose:

    ```
    sudo docker-compose up -d -build
    ```

This will call the `Dockerfile` of each subfolder as well as other services such as postGIS and pg_tileserv. The following containers and services should be built:

| container/service   | Port(s)                               | Purpose                                                                                       |
|---------------------|---------------------------------------|-----------------------------------------------------------------------------------------------|
| data-backend        | N/A                                   | Python data processing backend to get UPCARE API sensor data, perform Kriging interpolation to street-level AQI, and save it into the PostGIS database. |
| express-leaflet     | localhost:3000                        | Leaflet application to show air quality map and routing results.                              |
| tileserv            | localhost:7800                        | Tile server for PostGIS database to serve street-level AQI data.                              |
| postgis             | postgres://<user>:<password>@db:5432/manila_osm | PostGIS database server to store street-level interpolated AQI data.                          |
| routing-backend     | localhost:9098                        | GraphHopper router server. See `/routing-backend/router_API.yaml` for OpenAPI specifications.  |

Note that the routing-backend may take some time to initialize the GraphHopper routing network. Check its logs and wait for it to finish.