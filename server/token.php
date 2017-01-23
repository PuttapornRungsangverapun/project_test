<?php
include("db_con.php");
header('Content-type:application/json');
$token = $_REQUEST["token"];
$user_id = $_REQUEST["userid"];

$token_qry = "select user_id,token_body from tokens where user_id = '$user_id' and token_body='$token'";
$result_token = mysqli_query($conn ,$token_qry);
$row_token=mysqli_fetch_array($result_token);
$ret=array();
if(!$row_token){
	http_response_code(403);
	$ret['message']="You don't have permission to access";
	echo json_encode($ret);
	exit();
}
	
?>