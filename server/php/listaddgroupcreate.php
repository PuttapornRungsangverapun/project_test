<?php 
include("db_con.php");
header('Content-type:application/json');
include('token.php');
//$user_id = $_REQUEST["userid"];
/*$mysql_qry = "select users.user_id,users.user_username,users.user_publickey publickey
from users inner join friends on users.user_id=friends.friend_id
where friends.user_id = '$user_id'";*/

$mysql_qry2="SELECT f1.friend_id, u.user_username FROM friends f1 join friends f2 on f1.user_id = f2.friend_id and f1.friend_id = f2.user_id left join users u on f1.friend_id = u.user_id where f1.user_id = '$user_id'";
$result2 = mysqli_query($conn ,$mysql_qry2);
//$row=mysqli_fetch_array($result);
//$row=mysqli_fetch_all($result);
$lvname=array();
while($row=mysqli_fetch_assoc($result2)){
	$lvname[]=$row;
	
	}
 $ret['status']="success";	
$ret['message']=$lvname;

echo json_encode($ret);

?>
