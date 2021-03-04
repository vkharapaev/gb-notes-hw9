package com.headmostlab.notes.ui.notelist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.headmostlab.notes.model.Note;

import java.util.ArrayList;
import java.util.Date;

public class NoteListViewModelImpl extends androidx.lifecycle.ViewModel implements NoteListViewModel {

    public static final String NOTE_KEY = "NOTE";
    private final SavedStateHandle dataStorage;
    private MutableLiveData<ArrayList<Note>> notesLiveData = new MutableLiveData<>();
    private MutableLiveData<Note> selectedNote = new MutableLiveData<>();
    private MutableLiveData<Integer> deletedNote = new MutableLiveData<>();

    public NoteListViewModelImpl(SavedStateHandle savedState) {
        loadNotes();
        dataStorage = savedState;
        Note note = savedState.get(NOTE_KEY);
        if (note != null) {
            selectedNote.setValue(note);
        }
    }

    public LiveData<ArrayList<Note>> getNotes() {
        return notesLiveData;
    }

    @Override
    public LiveData<Note> getSelectedNote() {
        return selectedNote;
    }

    @Override
    public void selectNote(int position) {
        Note note = notesLiveData.getValue().get(position);
        selectedNote.setValue(note);
        dataStorage.set(NOTE_KEY, note);
    }

    @Override
    public void deselect() {
        selectedNote.setValue(null);
    }

    @Override
    public void deleteNote() {
        Note note = selectedNote.getValue();
        if (note != null) {
            ArrayList<Note> notes = notesLiveData.getValue();
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).getId().equals(note.getId())) {
                    notes.remove(i);
                    notesLiveData.setValue(notes);
                    selectedNote.setValue(null);
                    break;
                }
            }
        }
    }

    @Override
    public void updateNote(Note note) {
        if (note != null) {
            ArrayList<Note> notes = notesLiveData.getValue();
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).getId().equals(note.getId())) {
                    notes.remove(i);
                    notes.add(i, note);
                    notesLiveData.setValue(notes);
                    break;
                }
            }
        }
    }

    private void loadNotes() {
        notesLiveData.setValue(createNotes());
    }

    private ArrayList<Note> createNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            notes.add(new Note(String.valueOf(i), "Note " + i, "Note " + i + " Description", new Date()));
        }
        return notes;
    }
}
