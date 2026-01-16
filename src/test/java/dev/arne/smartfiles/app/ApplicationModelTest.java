package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationModelTest {

    private ApplicationModel model;

    @BeforeEach
    void setUp() {
        model = new ApplicationModel();
    }

    @Test
    void setSelectedDocument_setsDescriptionProperty() {
        var entry = createTestEntry("test.pdf", "Initial description");
        model.getDocumentsList().add(entry);

        model.setSelectedDocument(entry);

        assertEquals("Initial description", model.getDescriptionProperty().get());
    }

    @Test
    void updateDescription_updatesDescriptionProperty() {
        var entry = createTestEntry("test.pdf", "Initial description");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        model.updateDescription("Updated description");

        assertEquals("Updated description", model.getDescriptionProperty().get());
    }

    @Test
    void updateDescription_updatesEntryInDocumentsList() {
        var entry = createTestEntry("test.pdf", "Initial description");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        model.updateDescription("Updated description");

        var updatedEntry = model.getDocumentsList().getFirst();
        assertEquals("Updated description", updatedEntry.getSummary());
    }

    @Test
    void updateDescription_refreshesDocumentInList() {
        var entry = createTestEntry("test.pdf", "Initial description");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        var listChanged = new boolean[]{false};
        model.getDocumentsList().addListener((javafx.collections.ListChangeListener<ArchiveEntry>) c -> {
            listChanged[0] = true;
        });

        model.updateDescription("Updated description");

        assertTrue(listChanged[0], "Document list should have been updated");
    }

    @Test
    void updateDescription_whenNoDocumentSelected_doesNotThrow() {
        assertDoesNotThrow(() -> model.updateDescription("Some description"));
    }

    @Test
    void toggleFilterTag_addsNewTagToSelectedFilterTags() {
        var tag = new Tag("invoice");

        model.toggleFilterTag(tag);

        assertTrue(model.isFilterTagSelected(tag));
    }

    @Test
    void toggleFilterTag_removesExistingTagFromSelectedFilterTags() {
        var tag = new Tag("invoice");
        model.toggleFilterTag(tag);
        assertTrue(model.isFilterTagSelected(tag));

        model.toggleFilterTag(tag);

        assertFalse(model.isFilterTagSelected(tag));
    }

    @Test
    void isFilterTagSelected_returnsTrueForExistingTag() {
        var tag = new Tag("invoice");
        model.toggleFilterTag(tag);

        assertTrue(model.isFilterTagSelected(tag));
    }

    @Test
    void isFilterTagSelected_returnsFalseForNonExistingTag() {
        var tag = new Tag("invoice");

        assertFalse(model.isFilterTagSelected(tag));
    }

    @Test
    void setAllTags_replacesAllTagsAndCleansInvalidFilters() {
        var initialTags = Set.of(new Tag("invoice"), new Tag("receipt"));
        model.setAllTags(initialTags);
        model.toggleFilterTag(new Tag("invoice"));
        assertTrue(model.isFilterTagSelected(new Tag("invoice")));

        var newTags = Set.of(new Tag("receipt"));
        model.setAllTags(newTags);

        assertEquals(1, model.getAllTagsProperty().size());
        assertFalse(model.isFilterTagSelected(new Tag("invoice")));
    }

    @Test
    void filterPredicate_withSearchText_matchesDocumentNames() {
        model.getDocumentsList().add(createTestEntry("Invoice.pdf", ""));
        model.getDocumentsList().add(createTestEntry("Receipt.pdf", ""));
        model.getDocumentsList().add(createTestEntry("Summary.pdf", ""));

        model.getSearchTextProperty().set("invoice");

        assertEquals(1, model.getFilteredDocuments().size());
        assertEquals("Invoice.pdf", model.getFilteredDocuments().getFirst().getName());
    }

    @Test
    void filterPredicate_withSearchText_isCaseInsensitive() {
        model.getDocumentsList().add(createTestEntry("INVOICE.pdf", ""));
        model.getDocumentsList().add(createTestEntry("receipt.pdf", ""));

        model.getSearchTextProperty().set("invoice");

        assertEquals(1, model.getFilteredDocuments().size());
        assertEquals("INVOICE.pdf", model.getFilteredDocuments().getFirst().getName());
    }

    @Test
    void filterPredicate_withSelectedTags_matchesDocumentTags() {
        var entry1 = createTestEntry("doc1.pdf", "");
        entry1.getTags().add(new Tag("invoice"));
        var entry2 = createTestEntry("doc2.pdf", "");
        entry2.getTags().add(new Tag("receipt"));
        model.getDocumentsList().addAll(entry1, entry2);

        model.toggleFilterTag(new Tag("invoice"));

        assertEquals(1, model.getFilteredDocuments().size());
        assertEquals("doc1.pdf", model.getFilteredDocuments().getFirst().getName());
    }

    @Test
    void filterPredicate_withBothSearchAndTags_requiresBoth() {
        var entry1 = createTestEntry("Invoice-2024.pdf", "");
        entry1.getTags().add(new Tag("finance"));
        var entry2 = createTestEntry("Invoice-2023.pdf", "");
        entry2.getTags().add(new Tag("archive"));
        var entry3 = createTestEntry("Receipt.pdf", "");
        entry3.getTags().add(new Tag("finance"));
        model.getDocumentsList().addAll(entry1, entry2, entry3);

        model.getSearchTextProperty().set("invoice");
        model.toggleFilterTag(new Tag("finance"));

        assertEquals(1, model.getFilteredDocuments().size());
        assertEquals("Invoice-2024.pdf", model.getFilteredDocuments().getFirst().getName());
    }

    @Test
    void filterPredicate_withEmptySearchText_showsAllDocuments() {
        model.getDocumentsList().add(createTestEntry("doc1.pdf", ""));
        model.getDocumentsList().add(createTestEntry("doc2.pdf", ""));
        model.getSearchTextProperty().set("test");
        assertEquals(0, model.getFilteredDocuments().size());

        model.getSearchTextProperty().set("");

        assertEquals(2, model.getFilteredDocuments().size());
    }

    @Test
    void removeDocument_whenSelectedDocument_clearsSelection() {
        var entry = createTestEntry("test.pdf", "Test description");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);
        assertEquals("test.pdf", model.getSelectedDocumentNameProperty().get());

        model.removeDocument(entry.getId());

        assertNull(model.getSelectedDocumentProperty().get());
        assertNull(model.getSelectedDocumentNameProperty().get());
        assertEquals("", model.getDescriptionProperty().get());
        assertTrue(model.getTagsProperty().isEmpty());
    }

    @Test
    void removeDocument_whenNotSelectedDocument_doesNotClearSelection() {
        var entry1 = createTestEntry("doc1.pdf", "");
        var entry2 = createTestEntry("doc2.pdf", "");
        model.getDocumentsList().addAll(entry1, entry2);
        model.setSelectedDocument(entry1);

        model.removeDocument(entry2.getId());

        assertEquals(entry1, model.getSelectedDocumentProperty().get());
        assertEquals("doc1.pdf", model.getSelectedDocumentNameProperty().get());
    }

    @Test
    void clearSelectedDocument_clearsAllSelectionProperties() {
        var entry = createTestEntry("test.pdf", "Description");
        entry.getTags().add(new Tag("invoice"));
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        model.clearSelectedDocument();

        assertNull(model.getSelectedDocumentProperty().get());
        assertNull(model.getSelectedDocumentNameProperty().get());
        assertEquals("", model.getDescriptionProperty().get());
        assertTrue(model.getTagsProperty().isEmpty());
    }

    private ArchiveEntry createTestEntry(String name, String summary) {
        var entry = new ArchiveEntry();
        entry.setId(UUID.randomUUID());
        entry.setName(name);
        entry.setSummary(summary);
        entry.setPath("/tmp/" + name);
        entry.setAbsolutePath("/tmp/" + name);
        entry.setOriginalPath("/orig/" + name);
        entry.setTags(new HashSet<>());
        entry.setDateCreated(LocalDateTime.now());
        entry.setDateLastModified(LocalDateTime.now());
        return entry;
    }
}
