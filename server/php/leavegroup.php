<?php
include("db_con.php");
include("token.php");
header('Content-type:application/json');
$group_id = $_REQUEST["groupid"];

$mysql_qry = "DELETE FROM groups_users WHERE groups_users.group_id=? AND groups_users.user_id=?";

$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'ss',$group_id,$user_id);
mysqli_stmt_execute($result);


$ret['status']="success";
echo json_encode($ret);
?>