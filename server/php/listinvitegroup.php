<?php 
include("db_con.php");
header('Content-type:application/json');
include('token.php');
$group_id=$_REQUEST["groupid"];


$mysql_qry="SELECT f1.friend_id fid, u.user_username FROM friends f1 join friends f2 on f1.user_id = f2.friend_id and f1.friend_id = f2.user_id left join users u on f1.friend_id = u.user_id where f1.user_id = '$user_id'";
$result = mysqli_query($conn ,$mysql_qry);

	
//$row=mysqli_fetch_array($result);
//$row=mysqli_fetch_all($result);
$lvname=array();
while($row=mysqli_fetch_assoc($result)){
	
	$mysql_qry2="SELECT users.user_id FROM users LEFT JOIN groups_users ON users.user_id=groups_users.user_id WHERE groups_users.user_id='".$row['fid']."' and groups_users.group_id='$group_id'";
$result2 = mysqli_query($conn ,$mysql_qry2);
$row2=mysqli_fetch_array($result2);
	
	if(!$row2){
	$lvname[]=$row;
	}
	}
 $ret['status']="success";	
$ret['message']=$lvname;

echo json_encode($ret);

?>
