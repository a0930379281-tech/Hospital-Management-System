Hospital Management System — Eclipse Java Project (NO MAVEN)

Based on the directory style of the uploaded Wk9_Jdbc Eclipse project.

1. Eclipse > File > Import > General > Existing Projects into Workspace.
2. Select this extracted project folder.
3. Project > Properties > Java Build Path > Libraries > Classpath > Add External JARs.
   Add ALL javafx-sdk-25/lib/*.jar and mysql-connector-j-9.x.x.jar.
4. MySQL Workbench: execute database.sql.
5. Run > Run Configurations > Java Application > HospitalManagementApp.
   VM arguments: --module-path "C:\javafx-sdk-25\lib" --add-modules javafx.controls
   Environment: MYSQL_PASSWORD = your own local MySQL password.
6. Run the application and test Add, Show All, Update, Delete, Search, Clear.

Notes: The JAR paths are machine-specific, so they are NOT hardcoded in .classpath.
The app assumes local MySQL on port 3306 with user root.
Use fictional patient data only.
Submit screenshots and a short CRUD explanation as required by the assignment.
