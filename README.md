# SmartArchive

Archive your PDFs like with Evernote back then, but free and offline first. Everything works on your machine.
Access any file from the past in now time. Leverage local LLM models to help to categorize and summarize your documents.

## Build & Run

Requires Java 25.

```bash
# Run the application
./mvnw clean javafx:run

# Run tests
./mvnw test

# Package
./mvnw clean package
```

## Todos

- [x] Create basic app structure
- [x] Create and persist a model
- [x] Add files per Drag & Drop
- [x] Show list with managed files
- [x] Render the first page of selected document
- [ ] ~~Rename from SmartFiles to SmartArchive~~
- [x] Persist parts of the ApplicationModel on disk and read it together with the Archive
  - [x] Like the selected theme
- [x] Add a tag to a document
- [x] Render all pages of a document
- [ ] Show name, date and tags in Document details
- [x] Show all tags in the top-left panel and
- [x] Search for document name (show basic filtering of list)
- [x] Filter by tags
- [ ] Sorting by date / name (show basic sorting)
- [ ] Only now start with AI stuff
  - [ ] Suggest tags

