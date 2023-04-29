# codes.clj.docs/extractor

Tool to extract namespace/functions documentation from Clojure projects into indexed Datalevin file.

# CLI

## Extract and generate datalevin file
```bash
clojure -X:extract
```

## Update version of the configured projects in the config file
```bash
clojure -X:update
```

# Developing

## Repl
```bash
clojure -M:dev:nrepl
```

## Tests
```bash
clojure -M:dev:test
```
