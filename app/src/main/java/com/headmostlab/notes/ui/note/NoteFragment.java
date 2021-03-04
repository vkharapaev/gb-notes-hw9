package com.headmostlab.notes.ui.note;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.headmostlab.notes.R;
import com.headmostlab.notes.databinding.FragmentNoteBinding;
import com.headmostlab.notes.model.Note;
import com.headmostlab.notes.ui.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class NoteFragment extends Fragment {

    public static final String NOTE_KEY = "NOTE";
    private FragmentNoteBinding binding;
    private NoteViewModel viewModel;
    private boolean isPortrait;
    private OnBackPressedCallback onBackPressedCallback;
    private Note note;

    public static NoteFragment newNoteFragment(Note note) {
        NoteFragment fragment = new NoteFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(NOTE_KEY, note);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this,
                new NoteViewModelFactory(this, null)).get(NoteViewModelImpl.class);

        setHasOptionsMenu(true);

        isPortrait = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT;

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().setFragmentResult(Constants.FRAGMENT_RESULT_BACK_PRESS_IN_EDIT_NOTE, new Bundle());
                getParentFragmentManager().popBackStack();
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (!isPortrait) {
            getParentFragmentManager().popBackStack();
        }
        binding = FragmentNoteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onBackPressedCallback.remove();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            note = getArguments().getParcelable(NOTE_KEY);
            viewModel.setNote(note);
        }
        if (isPortrait) {
            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);
        }
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(selection ->
                binding.createDate.setText(DateFormat.getDateInstance().format(new Date(selection))));
        binding.createDate.setOnClickListener(v ->
                picker.show(getParentFragmentManager(), picker.toString()));
        binding.deleteNoteButton.setOnClickListener(it -> {
            getParentFragmentManager().setFragmentResult(Constants.FRAGMENT_RESULT_DELETE_NOTE, new Bundle());
            if (isPortrait) {
                getParentFragmentManager().popBackStack();
            } else {
                getParentFragmentManager().beginTransaction().remove(this).commit();
            }
        });
        binding.saveNoteButton.setOnClickListener(it -> {
            if (note != null) {

                Bundle bundle = new Bundle();

                Date date = null;
                try {
                    date = DateFormat.getDateInstance().parse(binding.createDate.getText().toString());
                } catch (ParseException ignore) {
                }

                Note updatedNote = new Note(this.note.getId(),
                        binding.title.getText().toString(),
                        binding.description.getText().toString(),
                        date
                );

                bundle.putParcelable(Constants.FRAGMENT_RESULT_NOTE, updatedNote);
                getParentFragmentManager().setFragmentResult(Constants.FRAGMENT_RESULT_UPDATE_NOTE, bundle);

                if (isPortrait) {
                    getParentFragmentManager().popBackStack();
                }
            }
        });
        viewModel.getSelectedNote().observe(getViewLifecycleOwner(), note -> show(note));
        viewModel.getNoteToShare().observe(getViewLifecycleOwner(), note -> share(note));
    }

    public void show(Note note) {
        if (note != null) {
            binding.title.setText(note.getTitle());
            binding.description.setText(note.getDescription());
            binding.createDate.setText(DateFormat.getDateInstance().format(note.getCreationDate()));
        }
    }

    public void share(Note note) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, note.toHumanString());
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share));
        startActivity(shareIntent);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.note_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            viewModel.share();
            return true;
        }
        return false;
    }
}
