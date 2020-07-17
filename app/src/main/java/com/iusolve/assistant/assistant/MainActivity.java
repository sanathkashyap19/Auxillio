package com.iusolve.assistant.assistant;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
    private EditText metTextHint;
    private ListView mlvTextMatches;
    private Spinner msTextMatches;
    private Button mbtSpeak;
    private PackageManager pm = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        metTextHint = (EditText) findViewById(R.id.etTextHint);
        mlvTextMatches = (ListView) findViewById(R.id.lvTextMatches);
        msTextMatches = (Spinner) findViewById(R.id.sNoOfMatches);
        mbtSpeak = (Button) findViewById(R.id.btSpeak);
        Button list = (Button) findViewById(R.id.list);
        Button loc = (Button) findViewById(R.id.loc);

        list.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListA.class));
            }
        });

        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GetLoc.class));
            }
        });
        checkVoiceRecognition();

    }
    public void checkVoiceRecognition() {

        // Check if voice recognition is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        if (activities.size() == 0) {

            mbtSpeak.setEnabled(false);
            mbtSpeak.setText("Voice recognizer not present");
            Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_SHORT).show();
        }

    }
    public void speak(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, metTextHint.getText().toString());

        // Given an hint to the recognizer about what the user is going to say
        //There are two form of language model available
        //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
        //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

        // If number of Matches is not selected then return show toast message
        if (msTextMatches.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {

            Toast.makeText(this, "Please select No. of Matches from spinner", Toast.LENGTH_SHORT).show();
            return;
        }
        //int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem()
                       // .toString());
        // Specify how many results you want to receive. The results will be
        // sorted where the first result is the one with higher confidence.
        //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
        //Start the Voice recognizer activity for the result.
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)

        //If Voice recognition is successful then it returns RESULT_OK
        if(resultCode == RESULT_OK) {
            ArrayList<String> textMatchList = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (!textMatchList.isEmpty()) {

                // If first Match contains the 'search' word
                // Then start web search.
                if (textMatchList.get(0).contains("search")) {

                    String searchQuery = textMatchList.get(0);
                    searchQuery = searchQuery.replace("search","");
                    Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
                    search.putExtra(SearchManager.QUERY, searchQuery);
                    startActivity(search);

                }
                else if(textMatchList.get(0).contains("open")) {

                    String open = textMatchList.get(0);
                    open  = open.replace("open", "");

                    showToastMessage("got open");

                    /*pm = this.getPackageManager();
                    List<ApplicationInfo> applist = checkForLaunchIntent(pm.getInstalledApplications
                            (PackageManager.GET_META_DATA));
                    showToastMessage("list preped");
                    for(int i=0; i<applist.size(); i++)
                    {
                        showToastMessage("searching");
                        if(pm.getApplicationLabel(applist.get(i)).equals(open))
                        {
                            Toast.makeText(this, "found match", Toast.LENGTH_LONG).show();
                            openApp(this, applist.get(i).toString());
                        }
                    }
                    Toast.makeText(this, "exit                ", Toast.LENGTH_LONG).show();*/

                    PackageManager packageManager = getPackageManager();
                    List<PackageInfo> packs = packageManager.getInstalledPackages(0);
                    int size = packs.size();
                    for (int i = 0; i < size; i++) {
                        PackageInfo p = packs.get(i);
                        String tmpAppName = p.applicationInfo.loadLabel(packageManager).toString();
                        String pname = p.packageName;
                        tmpAppName = tmpAppName.toLowerCase();
                        if (tmpAppName.trim().toLowerCase().equals(open.trim().toLowerCase())) {
                            PackageManager pm = this.getPackageManager();
                            Intent appStartIntent = pm.getLaunchIntentForPackage(pname);
                            if (null != appStartIntent) {
                                try {
                                    this.startActivity(appStartIntent);
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                else if(textMatchList.contains("add \\w \\w"))
                {
                    showToastMessage("add");
                    String add=null;
                    for(int i=0; i<textMatchList.size(); i++) {

                        add = textMatchList.get(i);
                        if(add.contains("add (\\w) (\\w)"))
                            break;
                    }

                    add = add.replace("add ", "");
                    String[] input = add.split(" ");
                    String name = input[0];
                    String num = input[1];
                    Intent addContact = new Intent(Intent.ACTION_INSERT);
                    addContact.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    addContact.putExtra(ContactsContract.Intents.Insert.NAME, name);
                    addContact.putExtra(ContactsContract.Intents.Insert.PHONE, num);
                    startActivity(addContact);

                }

                // populate the Matches
                mlvTextMatches.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textMatchList));
            }
            //Result code for various error.
        }
        else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR)
            showToastMessage("Audio Error");
        else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR)
            showToastMessage("Client Error");
        else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR)
            showToastMessage("Network Error");
        else if(resultCode == RecognizerIntent.RESULT_NO_MATCH)
            showToastMessage("No Match");
        else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR)
            showToastMessage("Server Error");
        super.onActivityResult(requestCode, resultCode, data);
    }
    void showToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if (null != pm.getLaunchIntentForPackage(info.name)) {
                    applist.add(info);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return applist;
    }

    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);
        return true;
    }

}