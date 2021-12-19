#Paidy Assignment
## Forex
### run in local
1. start one frame docker container
2. `sbt run`
### test
`sbt test`
### implementation detail
- The OneFrame service local docker will occupy 8080, so I set the listening port to 4080 for local test.
- The OneFrame service has a constraint that every token can query at most 1000 times a day.
  As a result, I implement a cache to keep the latest data in memory and set the cache to expire after 5 minutes.
- The OneFrame service return 200(ok) even when an error occurs.
  I have to deal with two kind of response for same response code.
  I also deal with other status code in case there is any edge case I didn't found out.
- The program layer will try to get all rate pairs everytime a new query comes in which renew all records in the cache if the asked record expired.