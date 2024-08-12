# BFST20Gruppe27

--- 
How to run the program
---
1. Open the program by double clicking on the jar file DanmarksKort.jar

if the above does not work, you can try to run it through commando prompt:
- Open CMD
- Navigate to the folder where the jar file is located
- Write the following in console:
  <path> java -jar DanmarksKort.jar
  
  Example:
  C:\Users\Jeppe\Desktop\Danmarkskort java -jar DanmarksKort.jar
  
Once the program has been run, you'll be prompted to select between loading one of the embedded binary files or to select and load your own file.
  
The program uses up to 5 gb memory when loading all of denmark.
Thus we have added an option to choose between loading a cutout map of Fyn, or all of Denmark. You will be prompted to select an option once you run the program.
To load all of Denmark, the amount of space must be available in order to run it, if it fails to load properly, you can try type the following in the commando prompt to allocate the needed memory:

  <Path> java -jar -Xmx6g DanmarksKort.jar
  
  Example: 
  C:\Users\Jeppe\Desktop\Danmarkskort java -jar -Xmx5g DanmarksKort.jar






--- 
Guide to using the program
---
1. Once the selected map has been loaded, the program should display the selected map
2. You can explore the map by holding left click on top of the map and dragging. You can zoom in and out using mousepad or scrollwheel.
3. In the left hand side of the program in the sidebar, you'll have the options to search for a route between two given addresses. The program will automatically suggests addresses as you type. You can use the arrow keys to navigate the suggested addresses or you can select one from the list with the mouse. Hitting enter simply selects the first on the list
4. After entering a source and destination address, you can select whether you want to travel by car, by bike or by walking.
5. You can then hit "Plan route" and the route will be planned and you'll see a list of travel directions below the button. At the very bottom you'll see the travel time and travel distance.

6. At the top you'll see a menubar. 
- In the file tab you can choose to load a new map or to save the current map.
- In the toggles tab, you can choose to enable illustrations for datastructures and algorithms used in the project. By toggling KDTree, you'll be able to see how the KDTree functions. By toggling Dijkstra, you'll be able to see the roads that the dijsktra algorithm visited when finding a route between your two searched addresses.
- In the themes tab you can switch to another color scheme, you can choose between the default theme, a google maps theme, and a dark theme.

7. In the search bar ontop of the map, you can search for a given address or city, and it will automatically take you there.
8. In the bottom right corner of the screen, you'll see a status bar that shows you several things:
- First and foremost, in the top of the status bar there's a scalebar indicating the current distance on the map.
- Just below the scalebar you will see the closest highway to your mouse.
- And just below that, you'll see the coordinates of your mouse current position.

9. The program also allows you to place point of interests. To do so, right click on the map and hit "Add point of interest", and you'll be prompted to give it a name.
10. To visit your previously placed points of interests, you can open the saved list in the top right corner of the program by clicking on the button named "POIs".
