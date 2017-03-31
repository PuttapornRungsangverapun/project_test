<?php
include("db_con.php");
header('Content-type:application/json');
include('token.php');
$group_id=$_REQUEST["groupid"];


$mysql_qry="SELECT f1.friend_id fid, u.user_username FROM friends f1 join friends f2 on f1.user_id = f2.friend_id and f1.friend_id = f2.user_id left join users u on f1.friend_id = u.user_id where f1.user_id = ?";
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$user_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);

$lvname=array();
while($row=mysqli_fetch_assoc($result)){
    
    $mysql_qry="SELECT users.user_id FROM users LEFT JOIN groups_users ON users.user_id=groups_users.user_id WHERE groups_users.user_id=? and groups_users.group_id=?";
    $result2 = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result2,'ss',$row['fid'],$group_id);
    mysqli_stmt_execute($result2);
    $result2= mysqli_stmt_get_result($result2);
    $row2=mysqli_fetch_array($result2);
    
    if(!$row2){
        $lvname[]=$row;
    }
}
$ret['status']="success";
$ret['message']=$lvname;

echo json_encode($ret);

?>