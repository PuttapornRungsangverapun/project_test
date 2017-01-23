<?php
include('db_con.php');
header('Content-type:application/json');
include('token.php');
$hash= $_REQUEST["md5"];
$mysql_qry = "select status from virus_hash where md5 = '$hash' and status='A'";
$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
$ret = array();
if($row){
$ret['status']="fail";	
$ret['message']="Send file fail";
	}
	else{
		$ret['status']="success";	
		}
		echo json_encode($ret); 
?>
