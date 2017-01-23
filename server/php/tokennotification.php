<?php
include('db_con.php');
header('Content-type:application/json');
include('token.php');

$user_id = $_REQUEST["userid"];
$token_noti= $_REQUEST["token_noti"];
$mysql_qry = "select * from tokens_notification where user_id=$user_id";
$result=mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
$ret=array();
if($row){
	$mysql_qry2 = "update tokens_notification set token_body=$token_noti where user_id=$user_id";
$row2=mysqli_query($conn ,$mysql_qry2);
$ret['status']="updatesuccess";	

}else{

$mysql_qry3 = "insert into tokens_notification(token_body,user_id) values ('$token_noti','$user_id')";
$row3=mysqli_query($conn ,$mysql_qry3);
$ret['status']="createsuccess";	
}
echo json_encode($ret);	
?>