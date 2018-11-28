# cls-sch-pipe [![Build Status](https://travis-ci.com/ChristopherDavenport/cls-sch-pipe.svg?token=PjFgW8EghY9gMM1rAphk&branch=master)](https://travis-ci.com/ChristopherDavenport/cls-sch-pipe)

Create `/lib` folder and place `ojdbcX.jar` there to make sure the oracle library is present.

Once complete the application can be deployed as a binary via  `sbt universal:packageBin` in which case you can find the
zip file with the distribution in `/target/universal/cls-sch-pipe-VERSION.zip`. Additionally if you would like to
build with docker you can instead publish locally with `sbt docker:publishLocal`.

## Configuration

The set of configs is a priority ordering.

Defaults <  Environmental Variables < Default Config File < Config File Set Via Command Line < Command Line Arguments

### Environmental Configuration

- `ORACLE_USER`
- `ORACLE_PASS`
- `ORACLE_HOST`
- `ORACLE_PORT`
- `ORACLE_SID`
- `ORACLE_DRIVER` - Optional
- `ORACLE_JDBC_URL` - Optional

- `POSTGRES_USER`
- `POSTGRES_PASS`
- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `POSTGRES_SID` - Optional
- `POSTGRES_DRIVER` - Optional
- `POSTGRES_JDBC_URL` - Optional

### File Configuration

There is a default location for a yaml file to configure the application, at `usr/local/etc/cls-sch-pipe.yml`

The file should have the following formats. Neither header can be omitted, but any individual field may be.

```yaml
oracle:
  host: "remotehost"
  port: 1521
  sid: "db"
  username: "user"
  password: "passwrod"
postgres:
  host: "localhost"
  post: 5432
  sid: "db"
  username: "user"
  password: "password"
```

### Command Line Configuration

Command Line Configuration should hopefully be straight forward.

```help
Usage: cls-sch-pipe [--oracle-user <string>] [--oracle-password <string>]
[--oracle-host <string>] [--oracle-port <integer>] [--oracle-sid <string>]
[--oracle-driver <string>] [--oracle-jdbc-url <string>] [--postgres-user <string>]
[--postgres-password <string>] [--postgres-host <string>] [--postgres-port <integer>]
[--postgres-sid <string>] [--postgres-driver <string>] [--postgres-jdbc-url <string>]
[--file <path>]

Run Database pipe.

Options and flags:
    --help
        Display this help text.
    --oracle-user <string>
        The oracle user to use
    --oracle-password <string>
        The oracle password to use
    --oracle-host <string>
        The oracle host to connect to
    --oracle-port <integer>
        The oracle port to connect to
    --oracle-sid <string>
        The oracle sid to connect to
    --oracle-driver <string>
        The oracle driver to user, only necessary using something custom
    --oracle-jdbc-url <string>
        The oracle jdbc url, only necessary to do something custom
    --postgres-user <string>
        The postgres user to use
    --postgres-password <string>
        The postgres password to use
    --postgres-host <string>
        The postgres host to connect to
    --postgres-port <integer>
        The postgres port to connect to
    --postgres-sid <string>
        The postgres sid to connect to
    --postgres-driver <string>
        The postgres driver to user, only necessary using something custom
    --postgres-jdbc-url <string>
        The postgres jdbc url, only necessary to do something custom
    --file <path>, -f <path>
        Path to Additional Configuration File
```
