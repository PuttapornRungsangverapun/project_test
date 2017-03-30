<?php 


include("db_con.php");
include('token.php');
header('Content-type:application/octet-stream');

$message_id = $_REQUEST["messageid"];
$mysql_qry = "select file_body,file_filename from groups_messages_files where group_message_id=?";
//$result = mysqli_query($conn ,$mysql_qry);
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$message_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
$row=mysqli_fetch_assoc($result);

$filename= $row['file_filename'];


$iv=substr($row['file_body'],0,16);
$bin = base64_decode(substr($row['file_body'],16));


header('Content-length:'.strlen($iv.$bin));
header('Content-Disposition:attachment;filename="'.$filename.'"');
//echo $filename;
echo $iv.$bin;
?>