<!--- 
Copyright © 2023 
Yevhen Levchenko ylevchenko@solvd.com
Sviatoslav Paseka spaseka@solvd.com
Mihail Belyuk mbelyuk@solvd.com
-->

# Performance testing framework
This framework could be used for backend load testing with Gatling load-tool and Lighthouse for web UI performance testing.
With Grafana open-source visualisation solution you can retrieve real-time metrics for Gatling tests and Prometheus host resources monitoring.

## Getting Started

Framework consists of next services:
- **Grafana**: data visualization & monitoring
- **Influxdb**: time series DB platform for metrics & events (Time Series Data)
- **Jenkins**: continuous integration server for tests execution
- **Gatling**: tool for backend load testing
- **Lighthouse**: tool for web UI performance testing
- **Prometheus**: time series DB platform for storing Host (Docker) metrics. Depends on:
- **cAdvisor**: analyzes and exposes resource usage and performance data from running containers

## Prerequisites

To run framework install docker: https://docs.docker.com/engine/installation/

You should be able to run ```docker run hello-world``` with no errors.

## Installing

1. Clone this repository
   ```git clone https://github.com/yevle/gatling-performance-infra.git```
2. open gatling-performance-infra dir

3. ```docker-compose up --build -d```

All containers should be up and running

### Services endpoints
- **jenkins** localhost:8181
- **grafana** localhost:8857
- **influxdb** localhost:8653
- **prometheus** localhost:9091
- **cadvisor** localhost:8081

## Jenkins

Login to Jenkins with admin/admin(could be changed in docker-compose file).
By default, jenkins consists of 3 jobs:
- **GatlingBackend**: runs Gatling simulation with only parameter: -SIMULATION, to set simulation class name.
- **GatlingBackendParam**: runs Gatling simulation with runtime parameters: -SIMULATION -USERS -RAMPUP -DURATION

- **Lighthouse**: runs load simulation on defined web service with runtime parameter: -SLACK_KEY

## Making your gatling script compatible with framework

Framework accept both .java and .scala versions of scripts. For default GatlingBackendParam job you can add next runtime parameters:
- -USERS
- -RAMPUP
- -DURATION

Before project building, you need to place simulation files in named directory inside 'gatling/simulations/' folder. Also all needed resources should be placed inside 'gatling/resources/' folder.

## Running scenario with GatlingBackendParam job

To run Gatling demo script:

**Open GatlingBackendParam job -> Build with Parameters -> Set simulation file name (<foldername.Filename>) -> Set build parameters  -> Build**

This job will start Gatling docker container and execute defined Gatling load simulation.

You'll find default test reports inside **Gatling** tab in Jenkins job page: 
http://localhost:8181/job/GatlingBackend/gatling/ 

Also you'll be able to retrieve real-time and post-run metrics on Grafana dashboard. 
Runtime metrics link is available in job description as well as post-run link will be attached to every runs description on finish.

## Grafana

You can login to Grafana with admin/admin and edit any dashboard.

### Available Gatling metrics

**1. Gatling Report Metrics** dashboard has multiple rows with different metrics:
- Table: Overall statistics for each request (Total requests count; OK and KO statuses count; Max / Min / Mean response time and percentiles; Std deviation). 
- Graph: Responses Status over Active Users (Shows count of OK/KO responses as well as users count each second)
- Graph: Requests Response Time (Shows changes in Min/Max/Mean response timings)
- Table: Errors (List of requests error messages)

Dashboard has "simulation" and "request" variables to choose which simulation metrics to show and which specific requests details.

**2. Gatling Report Comparison** dashboard has pairs of rows and graphs with metrics to compare two separate builds:
- Tables: Overall statistics for all requests or one chosen request stats (Total requests count; OK and KO statuses count; Max / Min / Mean response time and percentiles; Std deviation).
- Graphs: Responses Status over Active Users (Shows count of OK/KO responses as well as users count each second)
- Graphs: Requests Response Time (Shows changes in Min/Max/Mean response timings)

You need to choose builds to compare with variables: *Recent Build* and *Previous Build*.

**IMPORTANT:** you should only choose most resent build fist, and older build as previous.

After you apply new time range by clicking ```Zoom to data``` button on first graph, all the data will be correctly shown.

If second ("previous") build has longer duration than first ("recent"), you need to manually add time difference to dashboards' global time range, to see both graphs in full.

### Prometheus Metrics

**Docker monitoring with service selection** dashboard shows real time host performance metrics.
Dashboard contains visualizations based on data from Prometheus and cAdvisor. 
On dashboard you can choose "service" variable for according container stats to be shown.

### Timerange

All values in visualizations are calculated according to selected time range. Default timerange is last 5 min with 5 sec refresh. Timerange could be set in timepicker or selected on any graph.
