### RNArtist 1.1.1

* Fixed: sync issues between the sequence and the bracket notation fields
* Fixed: sync issues between the drawing and its preview in the database panel when some line widths became too thick. Some interactions present in the drawing disappears in the preview. If the 2D was reloaded from this save, these interactions were not drawn anymore. This was an issue in the RNArtistcore engine.
* Modified: the cumulative mouse clicks on an RNA object in the canvas 2D has a different behavior. After each click, the selection extends further from the first element selected.
* Added: RNArtist needs now a confirmation before to extract and save a 2D domain in the current folder DB
* Added: up/down arrow keys allow to extend/reduce a selection

### RNArtist 1.1.0

* Added: a bracket notation panel to create/edit a 2D as a bracket notation. It can be generated from scratch, from a file or from the current 2D displayed in the canvas (even parts of it).
* Added: the current drawing can be saved in different folders of the database. This allows to save different versions of the same drawing
* Added: the current selection can be recovered from the 2D canvas in order to use it as a selection criteria in the selection panel
* Added: a new toolbar attached to the canvas 2D. It provides two buttons: one to delete the current drawing and another one to display the DB folder in which this 2D has been saved
* Modified: when a folder is selected in the tree view of the database, its content is automatically loaded (if any). Consequently, the icon to load a folder has been removed grom the UI
* Fixed: tasks in the background are faster

### RNArtist 1.0.9

* Fixed: maximum memory increased for the java platform (-Xmx2048m)

### RNArtist 1.0.8

* Fixed: all the file paths have been made compatible with Windows OS
* Added: the last step for the quickstart tutorial 

### RNArtist 1.0.7

* Added: the documentation page for the junction layout panel.
* Fixed: after its launch, RNArtist is showing the documentation panel. But the corresponding button for this panel was not highlighted.
* Fixed: during the construction of the tree view for a database, RNArtist doesn't go beyond a new folder to be displayed (a folder not already stored in the data structure of the tree view). Consequently, its subfolders (if any) were not displayed.