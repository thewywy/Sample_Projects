namespace AccountTypes
{
    class Client
    {
        abstract class Account
        {
            public abstract string accountType {get;}
            public abstract byte servicesOffered {get;}
            public int minutes;
            public int limit;

            public abstract double calculateCost();
            public static double applyTaxesAndFees(double cost)
            {
                const double stateTax = .05;
                const double fedTax = .0875;
                const int serviceFee = 10;
                const int armAndLegFee = 20;
                return cost + (cost * stateTax) + (cost * fedTax) + serviceFee + armAndLegFee;
            }

            public static double calculateOverage(int limit, int mins, double costPerMin)
            {
                if (limit < mins)
                {
                    return (mins - limit) * costPerMin;
                }
                else
                {
                    return 0;
                }
            }

            public static Dictionary<byte, string> offerings = new Dictionary<byte, string>();
            public static byte noAdds = 0x80;
            public static byte premiumContent = 0x40;
            public static byte unlimitedTime = 0x20;
            public static byte monthlyDrawing = 0x10;
            public static byte customizedService = 0x08;
            public static byte highSpeed = 0x04;
            public static byte homeVisit = 0x02;
            public static byte seatAtBoard = 0x01;
            public static void populateOfferings()
            {
                offerings.Add(noAdds, "No Adds");
                offerings.Add(premiumContent, "Premium Content");
                offerings.Add(unlimitedTime, "Unlimited Time");
                offerings.Add(monthlyDrawing, "Monthly Drawing");
                offerings.Add(customizedService, "Customized Service");
                offerings.Add(highSpeed, "High Speed");
                offerings.Add(homeVisit, "Home Visit");
                offerings.Add(seatAtBoard, "Seat At Board");
            }

            public static void printServices(byte paidFor) {
                foreach (KeyValuePair<byte, string> offering in offerings)
                {
                    if((offering.Key&paidFor) == offering.Key)
                    {
                        Console.WriteLine(offering.Value);
                    }
                }
                Console.WriteLine();
            }
    }

        class Free : Account
        {
            public Free(int minutes)
            {
                this.minutes = minutes;
                this.limit = 100;
            }

            public override string accountType
            {
                get { return "Free"; }
            }

            public override byte servicesOffered
            {
                get{return 0;}
            }

            public override double calculateCost()
            {
                return calculateOverage(limit, minutes, .99);
            }
        }

        class Basic : Account
        {
            public Basic(int minutes)
            {
                this.minutes = minutes;
                this.limit = 200;
            }

            public override string accountType
            {
                get { return "Basic"; }
            }

            public override byte servicesOffered
            {
                get { return (byte)(noAdds| highSpeed); }
            }

            public override double calculateCost()
            {
                double cost = 9.99;
                cost += calculateOverage(limit, minutes, .49);
                return applyTaxesAndFees(cost);
            }
        }

        class Silver : Account
        {
            public Silver(int minutes)
            {
                this.minutes = minutes;
                this.limit = 300;
            }

            public override string accountType
            {
                get { return "Silver"; }
            }

            public override byte servicesOffered
            {
                get { return (byte)(noAdds | highSpeed | premiumContent); }
            }

            public override double calculateCost()
            {
                double cost = 19.99;
                cost += calculateOverage(limit, minutes, .19);
                return applyTaxesAndFees(cost);
            }
        }

        class Gold : Account
        {
            public Gold(int minutes)
            {
                this.minutes = minutes;
                this.limit = 400;
            }

            public override string accountType
            {
                get { return "Gold"; }
            }

            public override byte servicesOffered
            {
                get { return (byte)(noAdds | highSpeed | premiumContent | monthlyDrawing | homeVisit); }
            }

            public override double calculateCost()
            {
                double cost = 29.99;
                int rewards = 5;
                cost += calculateOverage(limit, minutes, .05);
                if(limit > minutes)
                {
                    cost -= rewards;
                }
                return applyTaxesAndFees(cost);
            }
        }

        class Platinum : Account
        {
            public Platinum(int minutes)
            {
                this.minutes = minutes;
            }
            public override string accountType
            {
                get { return "Platinum"; }
            }

            public override byte servicesOffered
            {
                get { return (byte)(noAdds | unlimitedTime | highSpeed | premiumContent | monthlyDrawing | homeVisit | seatAtBoard); }
            }

            public override double calculateCost()
            {
                const double cost = 59.99;
                return applyTaxesAndFees(cost);
            }
        }

        static class accountFactory
        {
            public static Account Get(int id, int mins)
            {
                switch (id)
                {
                    case 0:
                        return new Free(mins);
                    case 1:
                        return new Basic(mins);
                    case 2:
                        return new Silver(mins);
                    case 3:
                        return new Gold(mins);
                    case 4:
                        return new Platinum(mins);
                    default:
                        return new Free(mins);
                }
            }
        }

        static void Main() //generates dummy account types and prints them.
        {
            Account.populateOfferings();
            Random rnd = new Random();
            for (int i = 0; i < 5; i++)
            {
                var account = accountFactory.Get(i, rnd.Next((i+1)*80, (i+1)*120));
                Console.WriteLine(account.accountType + " Account");
                Console.WriteLine("*************************\nAdvanced Services:");
                Account.printServices(account.servicesOffered);
                Console.WriteLine("Minutes: {0} \n", account.minutes);
                Console.WriteLine("Cost: " + account.calculateCost().ToString("C") + "\n\n");
            }
            Console.Read();
        }
    }

}