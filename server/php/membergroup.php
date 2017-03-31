<?php 
include("db_con.php");
header('Content-type:application/json');
include('token.php');
$group_id=$_REQUEST["groupid"];

$mysql_qry="SELECT users.user_username
FROM users LEFT JOIN groups_users ON users.user_id=groups_users.user_id
WHERE groups_users.group_id=?";
// $result = mysqli_query($conn ,$mysql_qry);
//$row=mysqli_fetch_array($result);
//$row=mysqli_fetch_all($result);

$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$group_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);

$lvname=array();
while($row=mysqli_fetch_assoc($result)){
	$lvname[]=$row;
	
	}
 $ret['status']="success";	
$ret['message']=$lvname;

echo json_encode($ret);	
?>
