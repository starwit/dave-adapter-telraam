# Docs how to access Telraam API
These are some sample API calls for the Telraam API. See their [interactive spec](https://app.swaggerhub.com/apis-docs/telraam/Telraam-API/1.2.0) for more details.

## Get data for your city


1. Create a geo-fence (e.g. [here](https://www.badgeapps.com/geofence))
2. Get API Token
```bash
export APITOKEN=token
```
3. Query Telraam for all devices in this region

__request__
```bash
curl -X 'POST' \
  'https://telraam-api.net/v1/segments/area' \
  -H 'accept: */*' \
  -H "X-Api-Key: ${APITOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{
  "polygon": {
    "type": "Polygon",
    "coordinates": [
      [
        [
          10.884599,
          52.476114
        ],
        [
          10.670709,
          52.476114
        ],
        [
          10.670709,
          52.365772
        ],
        [
          10.884599,
          52.365772
        ]
      ]
    ]
  }
}'
```
__example response__
```json
{
  "status_code": 200,
  "message": "ok",
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "MultiLineString",
        "coordinates": [
          [
            [
              10.716889594,
              52.419578703
            ],
            [
              10.716879394,
              52.419857103
            ],
            [
              10.716891894,
              52.420138203
            ],
            [
              10.716915194,
              52.420470103
            ],
            [
              10.716910394,
              52.420535603
            ],
            [
              10.716894694,
              52.420662003
            ],
            [
              10.716876294,
              52.420793603
            ]
          ]
        ]
      },
      "properties": {
        "segment_id": 9000011165
      }
    }
  ]
}
```

4. Query each device

```bash
curl -X 'GET' \
  'https://telraam-api.net/v1/segments/id/9000011165' \
  -H 'accept: application/json' \
  -H "X-Api-Key: ${APITOKEN}"
```

__response__
```JSON
{
  "status_code": 200,
  "message": "ok",
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "MultiLineString",
        "coordinates": [
          [
            [
              10.716889594,
              52.419578703
            ],
            [
              10.716879394,
              52.419857103
            ],
            [
              10.716891894,
              52.420138203
            ],
            [
              10.716915194,
              52.420470103
            ],
            [
              10.716910394,
              52.420535603
            ],
            [
              10.716894694,
              52.420662003
            ],
            [
              10.716876294,
              52.420793603
            ]
          ]
        ]
      },
      "properties": {
        "oidn": 9000011165,
        "first_data_package": "2026-05-05T11:45:18.061Z",
        "last_data_package": "2026-05-21T15:30:26.044Z",
        "timezone": "Europe/Berlin",
        "instance_ids": {
          "14298": {
            "mac": 353785728305235,
            "manual": false,
            "status": "active",
            "private": false,
            "user_id": 13615,
            "time_end": null,
            "cars_left": true,
            "direction": false,
            "bikes_left": true,
            "cars_right": true,
            "time_added": "2026-05-05T11:36:31.187+00:00",
            "bikes_right": true,
            "status_detailed": "active",
            "hardware_version": 2,
            "pedestrians_left": true,
            "last_data_package": "2026-05-21T15:30:26.0449+00:00",
            "pedestrians_right": true,
            "first_data_package": "2026-05-05T11:45:18.061319+00:00",
            "is_calibration_done": "no"
          }
        }
      }
    }
  ]
}
```
5. Get traffic data
```bash
curl -X 'POST' \
  'https://telraam-api.net/v1/reports/traffic' \
  -H 'accept: application/json' \
  -H 'X-Api-Key: aBCKIhzB9ga4EY2Zy3MzS3x1mSGvQmLD4djU4eSd' \
  -H 'Content-Type: application/json' \
  -d '{
  "level": "segments",
  "id": "9000011165",
  "format": "per-hour",
  "time_start": "2026-05-21 11:00:00Z",
  "time_end": "2026-05-21 11:15:00Z"
}'
```
__response__
```json
{
  "status_code": 200,
  "message": "ok",
  "report": [
    {
      "instance_id": -1,
      "segment_id": 9000011165,
      "date": "2026-05-21T11:00:00.000Z",
      "interval": "hourly",
      "uptime": 0.9994444444,
      "heavy": 6.0,
      "car": 138.0,
      "bike": 12.0,
      "pedestrian": 50.0,
      "night": 0.0,
      "heavy_lft": 0.0,
      "heavy_rgt": 6.0,
      "car_lft": 42.0,
      "car_rgt": 96.0,
      "bike_lft": 2.0,
      "bike_rgt": 10.0,
      "pedestrian_lft": 20.0,
      "pedestrian_rgt": 30.0,
      "night_lft": 0.0,
      "night_rgt": 0.0,
      "direction": 1,
      "car_speed_hist_0to70plus": [
        2.1739130435,
        15.9420289855,
        28.2608695652,
        31.1594202899,
        13.768115942,
        7.2463768116,
        0.7246376812,
        0.7246376812
      ],
      "car_speed_hist_0to120plus": [
        0.0,
        2.1739130435,
        5.7971014493,
        10.1449275362,
        10.8695652174,
        17.3913043478,
        13.768115942,
        17.3913043478,
        10.1449275362,
        3.6231884058,
        3.6231884058,
        3.6231884058,
        0.7246376812,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.7246376812,
        0.0
      ],
      "timezone": "Europe/Berlin",
      "v85": 43.5
    }
  ]
}
```

