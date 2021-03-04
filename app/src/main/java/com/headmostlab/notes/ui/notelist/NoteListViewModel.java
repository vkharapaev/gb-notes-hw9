package com.headmostlab.notes.ui.notelist;

import androidx.lifecycle.LiveData;

import com.headmostlab.notes.model.Note;

import java.util.ArrayList;

public interface NoteListViewModel {
    LiveData<ArrayList<Note>> getNotes();

    LiveData<Note> getSelectedNote();

    void selectNote(int position);

    void deselect();

    void deleteNote();

    void updateNote(Note note);
}
