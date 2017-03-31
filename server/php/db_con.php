<?php
$db_name ="chat";
$mysql_username = "root";
$mysql_password="u3-test-db";
$server_name="localhost";
$conn = @mysqli_connect($server_name,$mysql_username,$mysql_password,$db_name)or die("Error".mysql_error());

?>