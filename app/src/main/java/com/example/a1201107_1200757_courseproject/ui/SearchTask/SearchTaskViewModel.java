package com.example.a1201107_1200757_courseproject.ui.SearchTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SearchTaskViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SearchTaskViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}