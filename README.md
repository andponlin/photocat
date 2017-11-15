# photocat

```photocat``` is a simple java application for producing (digital) quasi '[contact sheets](https://en.wikipedia.org/wiki/Contact_print)'.  A contact sheet is a print-media page of thumbnails representing a set of photographic images.  Essentially you provide a number of images and it produces a PDF containing pages of those images scaled down together with some meta-data.

An example of where this might be useful is in the archiving of photographs.  For example, an organization keeps photographs of each project and after the project has completed, they archive those photos away.  A contact sheet would then be a reference where they could, at a glance and in a relatively smaller PDF file, see what was stored into each archive.

## Contact / Author

Andrew Lindesay <apl@lindesay.co.nz> ([web](http://www.lindesay.co.nz/))

## License

Please see the LICENSE.TXT file for the license that governs the use of this software.

## Building

To build this software, you will need the following;

* An internet connection
* Java 8
* Apache Maven

To build;

```
mvn clean && mvn package
```

## Usage

The software can be run either from the command line or as a desktop application.  In either case, the same conceptual settings are used

### Settings Overview

#### Source Image / Movie Directory

This is the directory containing the images and movies that should appear in the contact sheet.

#### PDF Output File

This is the file where the output PDF will be written

#### XML Output File (optional)

This is the optional output of an intermediate XML file.  This XML file is a necessary resource for developing your own contact sheet format should you wish to.

#### ffmpeg Binary (optional)

This is the binary for ```ffmpeg``` which is used to create stills for movies that may be included in the source image / movie directory.  ```ffmpeg``` can be downloaded from [here](http://ffmpeg.org/).  If you do not install ```ffmpeg``` then movies' stills will not appear properly in the contact sheet.

#### Default Output Template / Custom Template

You can use this option to either use the simple built-in template or you can nominate your own template should you have created your own one.

#### Maximum Thumbnail Edge Size

This will define the maximum size of the images that are included in the contact sheet.  The larger this value is, the larger the contact sheet PDF file will be and the more memory will be required to produce the output PDF.

### Launch Command-line

To launch the software using a command line interface issue a command like this, correcting the actual path to the _jar_ file depending on your situation;

```
java -jar .../photocat-contactsheet-cli/target/photocat-contactsheet-cli-...jar
```

Running the software with no options will provide you with a list of options.

### Launch Graphical User interface

To launch the software using a graphical user interface, issue a command line like that shown below, correcting the actual path to the _jar_ file depending on your situation;

```
java -jar ..../photocat-contactsheet-desktop/target/photocat-contactsheet-desktop-...jar
```

## Using Your Own Template

If you want to make your own contact sheet template, this is possible. Use the "XML Output File" option and produce an example XML file from your data.  Now you can create an XSLT file that produces FO (formatting objects) output.  Internally, the FO is run through the [FOP](http://xmlgraphics.apache.org/fop/) tool to produce the PDF.

There is a command-line tool provided with FOP to process the XML file with your XSLT file and then process the resultant FO into FOP.  This can help with developing your XSLT file.

The default XSL file exists within the sources for this project.
