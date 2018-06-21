# android-reasoning
An example <a href="https://developer.android.com/studio/">Android Studio</a> project that uses <a href="https://github.com/lencinhaus/androjena">AndroJena</a> and an <a href="https://www.w3.org/TR/owl2-profiles/#Reasoning_in_OWL_2_RL_and_RDF_Graphs_using_Rules">OWL2 RL</a> ruleset for ontology reasoning.

The repository contains separate modules for the AndroJena, ARQoid and Lucenoid dependencies (see <a href="https://github.com/lencinhaus/androjena">here</a> for details), as well as the example "AndroidRules" project that already includes these modules. 

The project was created to be usable out-of-the-box - just point Android Studio towards the project folder (File > Open...) and select the AndroidRules project.

In case of issues, try File > Invalidate Caches / Restart > Invalidate and Restart. After starting, clean the project (Build > Clean Project) and re-sync (File > Sync Project with Gradle Files).

See <a href="http://www.linkeddata.mobi/software/owl2-rl-on-android/">here</a> for more info.
