<?php 
include("db_con.php");
header('Content-type:application/json');
include('token.php');
$friend_id = $_REQUEST["friendid"];
/*$mysql_qry = "select users.user_id,users.user_username,users.user_publickey publickey
from users inner join friends on users.user_id=friends.friend_id
where friends.user_id = '$user_id'";*/

$mysql_qry=" select users.user_id,users.user_publickey publickey from users where user_id = '$friend_id'";
$result = mysqli_query($conn ,$mysql_qry);
//$row=mysqli_fetch_array($result);
//$row=mysqli_fetch_all($result);
$lvname=array();
$row=mysqli_fetch_assoc($result);
 $ret['status']="success";	
$ret['message']=$row;

echo json_encode($ret);	
?>
