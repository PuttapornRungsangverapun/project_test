<?php 


include("db_con.php");
include('token.php');
header('Content-type:application/octet-stream');

$message_id = $_REQUEST["messageid"];
$mysql_qry = "select file_body,file_filename from messages_files where message_id=$message_id";

$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_assoc($result);

$filename= $row['file_filename'];


$iv=substr($row['file_body'],0,16);
$bin = base64_decode(substr($row['file_body'],16));


header('Content-length:'.strlen($iv.$bin));
header('Content-Disposition:attachment;filename="'.$filename.'"');
//echo $filename;
echo $iv.$bin;
?>