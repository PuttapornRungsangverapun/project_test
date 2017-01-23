<?php 
include("db_con.php");
header('Content-type:application/json');
$user_id = $_REQUEST["userid"];
$user_pass = sha1($_REQUEST["password"]);
$mysql_qry = "select users.user_id,users.user_username
from users inner join friends on users.user_id=friends.friend_id
where friends.user_id = '$user_id'";
$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
if($row){
if($row['user_password']==($user_pass)){
$ret['status']="success";
$ret['userid']=intval($row['user_id']);	
$ret['message']="Login success";
}
else{
$ret['status']="fail1";	
$ret['message']="Login fail";
}
}
else{
$ret['status']="fail2";	
$ret['message']="Login fail";
}
echo json_encode($ret);	
?>
