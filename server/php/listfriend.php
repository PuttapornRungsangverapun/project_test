<?php
include("db_con.php");
header('Content-type:application/json');
include('token.php');
//$user_id = $_REQUEST["userid"];
/*$mysql_qry = "select users.user_id,users.user_username,users.user_publickey publickey
from users inner join friends on users.user_id=friends.friend_id
where friends.user_id = '$user_id'";*/

$mysql_qry=" select users.user_id,users.user_username,users.user_publickey publickey,-1 as group_id, null as group_name,-1 as n
from users inner join friends on users.user_id=friends.friend_id
where (friends.user_id = ?)
UNION
SELECT -1 as user_id ,null as user_username,null as user_publickey,groups.group_id,groups.group_name, c.n
FROM groups left join (select group_id, count(*) as n from groups_users group by group_id) c on groups.group_id=c.group_id, groups_users
WHERE (groups_users.user_id=?)
and  (groups.group_id=groups_users.group_id)";
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'ss',$user_id,$user_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
//$row=mysqli_fetch_array($result);
//$row=mysqli_fetch_all($result);
$lvname=array();
while($row=mysqli_fetch_assoc($result)){
    $lvname[]=$row;
    
}
$ret['status']="success";
$ret['message']=$lvname;

echo json_encode($ret);



?>