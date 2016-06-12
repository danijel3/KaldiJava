# KaldiJava

This project contains mostly a set of classes which allow interfacing Kaldi from within Java. This is not a replacement for Kaldi (it does require a working installation of Kaldi on the computer), but it does make life easier in certain situations.

The idea is to replace the standard Bash/Perl/Python scripts that are used in the Kaldi project. A common use-case for this would be when you want to include Kaldi in some web environment pipeline. Another use may be when you simply want to make your life performing experiments easier or when you require proper unicode support.

## Installation tips

This project doesn't yet contain any detailed documentation.

Generally you need the following steps:

  1. Install Kaldi from http://kaldi-asr.org/
  2. Intall any other tool from the ones included in the classes
  3. Configure the Settings.java file with proper paths (or create a settings file and load it at start)
  4. Modify the main program class to do what ever you need.

## Who made this

This project was started ad the Polish-Japanese Academy of Information Technology in Warsaw, Poland.

