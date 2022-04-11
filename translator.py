# define a SQL object with attributes mapping to SQL keywords
import collections
from copy import deepcopy
import copy
from ctypes import Union

class SQL:
    def __init__(self) -> None:
        self.SELECT = []
        self.FROM = []
        self.WHERE = []
        self.GROUPBY = []
        self.ORDERBY = []
        self.HAVING = []
        self.UNION = []
        self.INTERSECT = []

    def printSQL(self):
        print(self.getSQL())
        
    # updated at 0404 13:21pm
    def getSQL(self):
        res = ''
        if len(self.UNION)>0:
            res = f'{self.UNION[0]}\nUNION\n{self.UNION[1]}'
        elif len(self.INTERSECT) > 0:
            res = f'{self.INTERSECT[0]}\nINTERSECT\n{self.INTERSECT[1]}'
        else:
            res += self.getSELECT()+'\n'
            res += self.getFROM()+'\n'
            if len(self.WHERE) > 0:
                res += self.getWHERE()+'\n'
            if len(self.GROUPBY) > 0:
                res += self.getGROUPBY()+'\n'
            if len(self.HAVING) > 0:
                res += self.getHAVING()+'\n'
            if len(self.ORDERBY) > 0:
                res += self.getORDERBY()+'\n'
        return res
    
    def getSELECT(self):
        if len(self.UNION) > 0:
            return ''
        if self.SELECT[0]=='DISTINCT':
            return 'SELECT DISTINCT '+','.join(self.SELECT[1:])
        else:
            return 'SELECT '+','.join(self.SELECT)

    def getFROM(self):
        return 'FROM '+','.join(self.FROM)

    def getWHERE(self):
        return 'WHERE '+'AND'.join(self.WHERE)

    def getGROUPBY(self):
        return 'GROUP BY '+','.join(self.GROUPBY)

    def getORDERBY(self):
        return 'ORDER BY '+','.join(self.ORDERBY)

    def getHAVING(self):
        return 'HAVING '+','.join(self.HAVING)

class Node:
    def __init__(self) -> None:
        self.operator = None
        self.sql = None

    def updateSQL(self):
        pass

class UnaryOperator(Node):
    def __init__(self, operator, attributes, child_operand = None):
        self.operator = operator
        self.attributes = attributes
        self.child_operand = child_operand
        self.sql = SQL()
        self.updateSQL()

    # for debugging
    def toString(self):
        print(self.operator)
        print(f'SELECT {self.sql.SELECT}')
        print(f'FROM {self.sql.FROM}')
        print(f'WHERE {self.sql.WHERE}')
        print('------------------')

    def updateSQL(self):
        if self.operator == 'rho':
            self.sql.SELECT.append('*')
            self.sql.FROM.append(self.attributes)
            return
        
        # Assign childnode's SQL object to itself
        if self.child_operand:
            self.sql = self.child_operand.sql
            
        if self.operator == 'pi':
            self.sql.SELECT = self.attributes.split(',')

        if self.operator == 'sigma':
                self.sql.WHERE.append(self.attributes)

        if self.operator == 'gamma':
            tri = self.attributes.split('@')
            
            groupby = tri[0]
            self.sql.GROUPBY.extend(groupby.split(','))
            if len(tri)==2:
                select = tri[1]
                self.sql.SELECT.extend(select.split(','))

            # having conditions are connected with logical annotations instead of ','
            if len(tri)==3:
                having = tri[2]
                self.sql.HAVING.append(having)
            
        if self.operator == 'tau':
            if len(self.sql.ORDERBY)>1:
                self.sql.ORDERBY.append(','.join(self.attributes))
            else:
                self.sql.ORDERBY.append(self.attributes)

        if self.operator == 'delta':
            self.sql.SELECT.insert(0, 'DISTINCT')

class BinaryOperator(Node):
    def __init__(self, operator, attributes, left_operand, right_operand):
        self.operator = operator
        self.attributes = attributes
        self.left_operand = left_operand
        self.right_operand = right_operand
        self.sql = SQL()
        self.updateSQL()

    # for debugging
    def toString(self):
        print(self.operator)
        print(f'SELECT {self.sql.SELECT}')
        print(f'FROM {self.sql.FROM}')
        print(f'WHERE {self.sql.WHERE}')
        print('------------------')

    def updateSQL(self):
        # tip: SQL object is empty at the beginning(construction)

        # get left childnode's SQL object
        left = self.left_operand.sql
        # get right childnode's SQL object
        right = self.right_operand.sql

        # judge whether left/right childnode is a nested subquery
        isLeftSubquery = False
        isRightSubquery = False
        # updated at 0404 13:20

        
        if self.left_operand.operator == 'pi' or self.left_operand.operator == 'gamma':
            left_FROM = f'({left.getSQL()}) temp'
            isLeftSubquery = True
        else:
            left_FROM = ','.join(left.FROM)
            if isinstance(self.left_operand, BinaryOperator):
                left_FROM = f'({left_FROM})'
            
        if self.right_operand.operator == 'pi' or self.right_operand.operator =='gamma':
            right_FROM = f'({right.getSQL()}) temp'
            isRightSubquery = True
        else:
            right_FROM = ','.join(right.FROM)
            if isinstance(self.right_operand, BinaryOperator):
                right_FROM = f'({right_FROM})'
            
        if self.operator == 'cartesian':
            self.sql.SELECT = ['*']
            self.sql.FROM.append(left_FROM+' CROSS JOIN '+right_FROM)

        if self.operator == 'naturaljoin':
            self.sql.SELECT = ['*']
            self.sql.FROM.append(left_FROM+' NATURAL JOIN '+right_FROM)
            
        if self.operator == 'innerjoin':
            self.sql.SELECT = ['*']
            self.sql.FROM.append(left_FROM+' INNER JOIN '+right_FROM+' ON '+self.attributes)
            
        if self.operator == 'leftouterjoin':
            self.sql.SELECT = ['*']
            self.sql.FROM.append(left_FROM+' LEFT OUTER JOIN '+right_FROM+' ON '+self.attributes)

        if self.operator == 'rightouterjoin':
            self.sql.SELECT = ['*']
            self.sql.FROM.append(left_FROM+' RIGHT OUTER JOIN '+right_FROM+' ON '+self.attributes)

        if self.operator == 'fullouterjoin':
            self.sql.SELECT = ['*']
            self.sql.FROM.append(left_FROM+' FULL OUTER JOIN '+right_FROM+' ON '+self.attributes)

        if self.operator == 'union':
            self.sql.UNION.extend([left.getSQL(),right.getSQL()])

        if self.operator == 'intersect':
            self.sql.INTERSECT.append([left.getSQL(),right.getSQL()])

        # updated at 0405 0:30am
        # update WHERE of current node's sql
        if isLeftSubquery and isRightSubquery:
            pass
        elif isLeftSubquery:
            self.sql.WHERE = right.WHERE
        elif isRightSubquery:
            self.sql.WHERE = left.WHERE
        else:
            self.sql.WHERE = list(set(left.WHERE + right.WHERE))

        # update GROUPBY of current node's sql
        if isLeftSubquery and isRightSubquery:
            pass
        elif isLeftSubquery:
            self.sql.GROUPBY = right.GROUPBY
        elif isRightSubquery:
            self.sql.GROUPBY = left.GROUPBY
        else:
            self.sql.GROUPBY = list(set(left.GROUPBY + right.GROUPBY))

        # update ORDERBY of current node's sql
        if isLeftSubquery and isRightSubquery:
            pass
        elif isLeftSubquery:
            self.sql.ORDERBY = right.ORDERBY
        elif isRightSubquery:
            self.sql.ORDERBY = left.ORDERBY
        else:
            self.sql.ORDERBY = list(set(left.ORDERBY + right.ORDERBY))

        # update HAVING of curernt node's sql
        if isLeftSubquery and isRightSubquery:
            pass
        elif isLeftSubquery:
            self.sql.HAVING = right.HAVING
        elif isRightSubquery:
            self.sql.HAVING = left.HAVING
        else:
            self.sql.HAVING = list(set(left.HAVING + right.HAVING))

def main():
    import time
    start = time.time()
    test = ['pi','author','leftouterjoin','d.age = c.age','rho','d',"innerjoin","b.book = c.book",'rho',"b",'rho',"c"]
    
    test = '#delta#pi#name#sigma#age>20#rho#students'.split('#')
    test = 'union#pi#i.i_id,s.w_id#rightouterjoin#i.i_id=s.i_id#rho#stocks s#rho#items i#pi#s.i_id#sigma#s.w_id=w.w_id AND w.w_city=\'Singapore\'#cartesian#rho#stock s#rho#warehouses w'.split('#')
    test = 'pi#s.w_id#gamma#s.w_id@@AVG(s.s_qty)>=550#rho#stocks s'.split('#')
    test = 'pi#s.i_id#sigma#s.w_id=w.w_id AND w.w_city=\'Singapore\'#cartesian#rho#stock s#rho#warehouses w'.split('#')
    test = 'pi#i.i_id,s.w_id#rightouterjoin#i.i_id=s.i_id#rho#stocks s#rho#items i'.split('#')
    test = 'pi#s.i_id#innerjoin#s.w_id=w.w_id#pi#w.w_id#sigma#w.w_city=\'Singapore\'#rho#warehouses w#rho#stock s'.split('#')
    test = 'pi#count(r.studentname)#sigma#max(r.studentid)<100#gamma#r.teacherid#rho#r.studentid,r.studentname,r.teacherid,r.teach\
ername#innerjoin#student.id<teacher.id#rho#student.id,student.name#rho#teacher.id,teacher.name'.split('#')
    test = '#tau#job_id#pi#job_id#gamma#job_id@AVG(salary)@AVG(salary)<10000#rho#employees'.split('#')
    test = '#gamma#c.ssn@COUNT(cc.number) as count#leftouterjoin#c.ssn=cc.ssn#rho#customers c#rho#credit_cards cc'.split('#')
    test = '#delta#gamma#c.ssn@COUNT(cc.number) as count#leftouterjoin#c.ssn=cc.ssn#rho#customers c#rho#credit_cards cc'.split('#')
    test = '#pi#student.*#sigma#sc.CId=’01’ AND sc.score<60#innerjoin#student.SID=sc.SId#rho#student#rho#sc'.split('#')
    test = '#pi#e.continent#gamma#e.continent@@SUM(c.population*e.percentage/100) >=1000000000#innerjoin#c.code=e.country#rho#country c#rho#encompasses e'.split('#')
    test = '#pi#c.first_name,c.last_name#gamma#c.ssn,c.first_name,c.last_name@@#sigma#cc.type = \'jcb\' and cc1.type=\'visa\'#innerjoin#cc.ssn=cc1.ssn#innerjoin#c.ssn=cc.ssn#rho#customers c#rho#credit_cards cc#rho#credit_cards cc1'.split('#')
    test = '#pi#student.SId,student.Sname,t1.sumscore,t1.coursecount#innerjoin#student.SId=t1.SId#rho#student#gamma#sc.SId@SC.SId,sum(sc.score)as sumscore ,count(sc.CId) as coursecount@#rho#sc'.split('#')
    test = '#pi#student.*,t1.avgscore#innerjoin#student.SId=t1.SId#rho#student#gamma#sc.SID@AVG(sc.score)as avgscore@AVG(sc.score)>=60#rho#sc'.split('#')
    test = '#pi#t1.SId#pi#student.SId,t.CId#sigma#sc.CId is null#cartesian#pi#student.name#rho#student#leftouterjoin#t1.SId=sc.SId and t1.CId=sc.CId#rho#sc#pi#sc.CId#sigma#sc.SId=’01’#rho#sc'.split('#')
    #test = '#pi#student.SId,student.Sname,t1.sumscore,t1.coursecount#innerjoin#student.SId=t1.SId#rho#student#gamma#sc.SId@SC.SId,sum(sc.score)as sumscore ,count(sc.CId) as coursecount@#rho#sc'.split('#')
    #test = '#pi#student.*,t1.avgscore#innerjoin#student.SId=t1.SId#rho#student#gamma#sc.SId@AVG(sc.score)as avgscore@AVG(sc.score)>=60#rho#sc'.split('#')
    #test = '#pi#name, class_id, age, sex#cartesian#rho#students#gamma#id@count(*)@#'.split('#')
    #tau#author@Desc#pi#author#leftouterjoin#d.age = c.age#rho#d#innerjoin#b.book = c.book#rho#b#pi#c.id#sigma#c.price>100#rho#c
    # variable name updated at 0405 0:33am

    # core rationale
    deque = copy.deepcopy(test)
    stack = collections.deque()
    unary_op_set = {"pi","rho","sigma","tau",'gamma',"delta"}
    join_op_set = {'cartesian','naturaljoin','innerjoin','leftouterjoin','rightouterjoin','fullouterjoin'}
    ui_op_set = {'union','intersect','minus'}
    while deque:
        item = deque.pop()
        if item in unary_op_set:
            if item != "rho":
                n1 = stack.pop()
                if n1.operator not in ui_op_set:
                    curr = UnaryOperator(item,test[index1],n1)
                    # curr.toString()

                # item in {'union','intersect'}:
                else: 
                    # union or intersect results must have an rename if it needs to be used 
                    tmp_Node = UnaryOperator(n1, 'new_name')
                    stack.append(tmp_Node)
                    curr = UnaryOperator(tmp_Node, test[index1])
                    # curr.toString()
                stack.append(curr)
            # item == 'rho':
            else:
                curr = UnaryOperator(item,test[index1])
                # curr.toString()
                stack.append(curr)


        elif item in join_op_set:
            n1 = stack.pop()
            n2 = stack.pop()
            curr = BinaryOperator(item,test[index1],n1,n2) 
            stack.append(curr)

        elif item in ui_op_set:
            index1 = test.index(item)
            n1 = stack.pop()
            n2 = stack.pop()
            curr = BinaryOperator(item,None,n1,n2)
            # curr.toString()
            stack.append(curr)

        else:
            index1 = test.index(item)
            
    # if input is valid, finally a root node is left in the stack, whose sql contains the SQL we want
    stack[0].sql.printSQL()
    end = time.time()
    print(end-start)
if __name__ == "__main__":
    main()
